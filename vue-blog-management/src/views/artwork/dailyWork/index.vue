<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import MyPagination from '@/components/MyPagination.vue'
import PermissionViewTip from '@/components/PermissionViewTip.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete,
  Upload, Hide, Position, Timer, Close, Check, ZoomIn, ZoomOut,
  Document, WarningFilled
} from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'
import {
  getDailyListApi,
  addDailyApi,
  getDailyDetailApi,
  updateDailyApi,
  updateDailyStatusApi,
  updateDailyTopApi,
  setDailyTimedApi,
  cancelDailyTimedApi,
  logicDeleteDailyApi
} from '@/api/daily'
import {
  DAILY_STATUS,
  DAILY_TYPE,
  getDailyStatusText,
  getDailyStatusType,
  getDailyStatusOptions,
  getDailyTypeText,
  getDailyTypeTag,
  computeDailyType
} from '@/constants/dailyConstants'

// ====================== 查询条件 ======================
const queryForm = reactive({
  content: '',
  type: '',
  isTop: '',
  status: '',
  createTime: [],
  begin: '',
  end: ''
})

watch(
  () => queryForm.createTime,
  (newVal) => {
    if (Array.isArray(newVal) && newVal.length === 2) {
      queryForm.begin = newVal[0]
      queryForm.end = newVal[1]
    } else {
      queryForm.begin = ''
      queryForm.end = ''
    }
  }
)

const statusOptions = getDailyStatusOptions()
const typeOptions = [
  { label: '纯文字', value: DAILY_TYPE.TEXT },
  { label: '图片', value: DAILY_TYPE.IMAGE },
  { label: '文件', value: DAILY_TYPE.FILE },
  { label: '图文混合', value: DAILY_TYPE.MIXED }
]

// ====================== 列表 ======================
const loading = ref(false)
const loadError = ref(false)
const dailyList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const getDailyList = async () => {
  loading.value = true
  loadError.value = false
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value
  }
  delete params.createTime

  try {
    const result = await getDailyListApi(params)
    if (result.code === 200 && result.data) {
      dailyList.value = result.data.rows || []
      total.value = Number(result.data.total) || 0
    } else {
      ElMessage.error(result.msg || '获取日常列表失败')
      dailyList.value = []
      total.value = 0
      loadError.value = true
    }
  } catch (error) {
    console.error('获取日常列表异常', error)
    ElMessage.error('网络请求失败，请稍后重试')
    dailyList.value = []
    total.value = 0
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.content = ''
  queryForm.type = ''
  queryForm.isTop = ''
  queryForm.status = ''
  queryForm.createTime = []
  queryForm.begin = ''
  queryForm.end = ''
  currentPage.value = 1
  getDailyList()
}

// ====================== 新增/编辑弹窗 ======================
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const contentForm = reactive({
  content: '',
  images: [],
  files: []
})

const imageFileList = ref([])
const fileFileList = ref([])

// URL 字符串 -> el-upload file-list
const splitUrlList = (str) => {
  if (!str) return []
  return String(str)
    .split(',')
    .map((url) => url.trim())
    .filter(Boolean)
    .map((url) => ({ name: getFileNameFromUrl(url), url }))
}

const getFileNameFromUrl = (url) => {
  try {
    const decoded = decodeURIComponent(String(url).split('?')[0])
    return decoded.split('/').filter(Boolean).pop() || '附件'
  } catch (e) {
    return String(url).split('/').filter(Boolean).pop() || '附件'
  }
}

// 计算当前表单类型（自动识别）
const currentType = () =>
  computeDailyType(
    imageFileList.value.map((f) => f.url).filter(Boolean),
    fileFileList.value.map((f) => f.url).filter(Boolean)
  )

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  contentForm.content = ''
  imageFileList.value = []
  fileFileList.value = []
  dialogVisible.value = true
}

const editDaily = async (row) => {
  editId.value = row.id
  try {
    const result = await getDailyDetailApi(row.id)
    if (result.code === 200 && result.data) {
      const detail = result.data
      isEdit.value = true
      contentForm.content = detail.content || ''
      imageFileList.value = splitUrlList(detail.images)
      fileFileList.value = splitUrlList(detail.files)
      dialogVisible.value = true
    } else {
      ElMessage.error(result.msg || '获取日常详情失败')
    }
  } catch (error) {
    console.error('获取日常详情异常', error)
    ElMessage.error('获取日常详情失败，请稍后重试')
  }
}

// 上传前校验
const IMAGE_MAX_SIZE = 10 * 1024 * 1024
const FILE_MAX_SIZE = 50 * 1024 * 1024

const beforeImageUpload = (file) => {
  if (!file.type || !file.type.startsWith('image/')) {
    ElMessage.warning('只能上传图片文件')
    return false
  }
  if (file.size > IMAGE_MAX_SIZE) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }
  return true
}

const beforeFileUpload = (file) => {
  if (file.size > FILE_MAX_SIZE) {
    ElMessage.warning('附件大小不能超过 50MB')
    return false
  }
  return true
}

const customUpload = (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  uploadApi(formData)
    .then((res) => {
      if (res.code === 200 && res.data) {
        options.onSuccess({
          name: getFileNameFromUrl(res.data),
          url: res.data
        })
      } else {
        ElMessage.error(res.msg || '上传失败')
        options.onError(new Error(res.msg || '上传失败'))
      }
    })
    .catch((err) => {
      console.error('上传异常', err)
      ElMessage.error('上传失败，请稍后重试')
      options.onError(err)
    })
}

// el-upload 上传成功后，将真实地址写回 file.url（否则图片会停留在 blob 预览地址）
const handleUploadSuccess = (response, uploadFile) => {
  if (response && response.url) {
    uploadFile.url = response.url
  }
}

const closeDialog = () => {
  if (submitLoading.value) return
  dialogVisible.value = false
}

// 提交（add 时根据 status 决定草稿/发布，edit 时保持原状态）
const submitForm = async (targetStatus) => {
  if (!contentForm.content || !contentForm.content.trim()) {
    ElMessage.warning('请输入日常内容')
    return
  }
  const imageUrls = imageFileList.value.map((f) => f.url).filter(Boolean)
  const fileUrls = fileFileList.value.map((f) => f.url).filter(Boolean)
  if (imageUrls.length === 0 && fileUrls.length === 0) {
    ElMessage.warning('请至少上传一张图片或一个附件')
    return
  }

  submitLoading.value = true
  try {
    let result
    if (isEdit.value) {
      result = await updateDailyApi({
        id: editId.value,
        content: contentForm.content.trim(),
        images: imageUrls,
        files: fileUrls
      })
    } else {
      result = await addDailyApi({
        content: contentForm.content.trim(),
        images: imageUrls,
        files: fileUrls,
        status: targetStatus
      })
    }

    if (result.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : targetStatus === DAILY_STATUS.PUBLISHED ? '发布成功' : '已保存为草稿')
      dialogVisible.value = false
      getDailyList()
    } else {
      ElMessage.error(result.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交日常异常', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// ====================== 删除 ======================
const selectedIds = ref([])
const handleSelectionChange = (val) => {
  selectedIds.value = val.map((item) => item.id)
}

const deleteDaily = (id) => {
  ElMessageBox.confirm('确认要删除该日常吗？删除后可到回收站恢复。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const result = await logicDeleteDailyApi([id])
      if (result.code === 200) {
        ElMessage.success('删除成功')
        if (dailyList.value.length === 1 && currentPage.value > 1) {
          currentPage.value -= 1
        }
        getDailyList()
      } else {
        ElMessage.error(result.msg || '删除失败')
      }
    })
    .catch(() => {})
}

const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要删除的日常')
    return
  }
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条日常吗？删除后可到回收站恢复。`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      const result = await logicDeleteDailyApi(selectedIds.value)
      if (result.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        getDailyList()
      } else {
        ElMessage.error(result.msg || '批量删除失败')
      }
    })
    .catch(() => {})
}

// ====================== 状态/置顶/定时 ======================
// 行级操作进行中标记，防止重复点击
const actionLoadingId = ref(null)

const withActionGuard = async (id, fn) => {
  if (actionLoadingId.value !== null) return
  actionLoadingId.value = id
  try {
    await fn()
  } catch (error) {
    console.error('日常操作异常', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    actionLoadingId.value = null
  }
}

const confirmAction = (message) =>
  ElMessageBox.confirm(message, '操作提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })

const changeStatus = (row, targetStatus) => {
  const confirmText =
    targetStatus === DAILY_STATUS.PUBLISHED
      ? row.status === DAILY_STATUS.OFFLINE
        ? '确认上架该日常？'
        : '确认立即发布该日常？'
      : targetStatus === DAILY_STATUS.OFFLINE
        ? '确认下架该日常？'
        : '确认修改该日常状态？'

  confirmAction(confirmText)
    .then(() =>
      withActionGuard(row.id, async () => {
        const result = await updateDailyStatusApi({ id: row.id, status: targetStatus })
        if (result.code === 200) {
          ElMessage.success('操作成功')
          getDailyList()
        } else {
          ElMessage.error(result.msg || '操作失败')
        }
      })
    )
    .catch(() => {})
}

const togglePrivate = (row) => {
  const isPrivate = row.status === DAILY_STATUS.PRIVATE
  const confirmText = isPrivate ? '确认取消该日常的私密状态？' : '确认将该日常设为私密？'
  confirmAction(confirmText)
    .then(() =>
      withActionGuard(row.id, async () => {
        const result = await updateDailyStatusApi({
          id: row.id,
          status: isPrivate ? DAILY_STATUS.PUBLISHED : DAILY_STATUS.PRIVATE
        })
        if (result.code === 200) {
          ElMessage.success(isPrivate ? '已取消私密' : '已设为私密')
          getDailyList()
        } else {
          ElMessage.error(result.msg || '操作失败')
        }
      })
    )
    .catch(() => {})
}

const toggleTop = (row) => {
  const actionText = row.isTop === 1 ? '取消置顶' : '置顶'
  confirmAction(`确认要${actionText}该日常？`)
    .then(() =>
      withActionGuard(row.id, async () => {
        const result = await updateDailyTopApi(row.id)
        if (result.code === 200) {
          ElMessage.success(`${actionText}成功`)
          getDailyList()
        } else {
          ElMessage.error(result.msg || '操作失败')
        }
      })
    )
    .catch(() => {})
}

// 定时发布
const timedDialogVisible = ref(false)
const currentTimedId = ref(null)
const timedPublishTime = ref('')
const timedSubmitLoading = ref(false)

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

const openTimedDialog = (id) => {
  currentTimedId.value = id
  timedPublishTime.value = ''
  timedDialogVisible.value = true
}

const submitSetTimed = async () => {
  if (!timedPublishTime.value) {
    ElMessage.warning('请选择定时时间')
    return
  }
  timedSubmitLoading.value = true
  try {
    const result = await setDailyTimedApi({
      id: currentTimedId.value,
      timedPublishTime: timedPublishTime.value
    })
    if (result.code === 200) {
      ElMessage.success('定时发布设置成功')
      timedDialogVisible.value = false
      getDailyList()
    } else {
      ElMessage.error(result.msg || '设置失败')
    }
  } catch (error) {
    console.error('设置定时异常', error)
    ElMessage.error('设置失败，请稍后重试')
  } finally {
    timedSubmitLoading.value = false
  }
}

const cancelTimed = (id) => {
  confirmAction('确认取消该日常的定时发布？取消后将恢复为草稿。')
    .then(() =>
      withActionGuard(id, async () => {
        const result = await cancelDailyTimedApi(id)
        if (result.code === 200) {
          ElMessage.success('已取消定时发布')
          getDailyList()
        } else {
          ElMessage.error(result.msg || '取消失败')
        }
      })
    )
    .catch(() => {})
}

// ====================== 预览 ======================
const previewDialogVisible = ref(false)
const previewRow = ref({})
const previewImages = ref([])

const viewDaily = (row) => {
  previewRow.value = { ...row }
  previewImages.value = splitUrlList(row.images).map((f) => f.url)
  previewDialogVisible.value = true
}

const previewFiles = computed(() => splitUrlList(previewRow.value.files))

// ====================== 图片大图预览 ======================
const showImageModal = ref(false)
const previewImageList = ref([])
const currentImgIndex = ref(0)
const scale = ref(2.3)
const imageLoading = ref(false)
const imageLoadError = ref(false)

const preloadImage = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(url)
    img.onerror = (err) => reject(err)
    img.src = url
  })
}

const preloadAdjacent = (list, currentIndex) => {
  if (!list || list.length === 0) return
  const prevIndex = currentIndex > 0 ? currentIndex - 1 : list.length - 1
  const nextIndex = currentIndex < list.length - 1 ? currentIndex + 1 : 0
  if (list[prevIndex]) preloadImage(list[prevIndex]).catch(() => {})
  if (list[nextIndex]) preloadImage(list[nextIndex]).catch(() => {})
}

const switchToImage = async (newIndex) => {
  if (imageLoading.value) return
  const len = previewImageList.value.length
  if (len === 0) return
  const safeIndex = (newIndex + len) % len
  const targetUrl = previewImageList.value[safeIndex]
  if (!targetUrl) return

  imageLoading.value = true
  imageLoadError.value = false
  try {
    await preloadImage(targetUrl)
    currentImgIndex.value = safeIndex
    preloadAdjacent(previewImageList.value, safeIndex)
  } catch (err) {
    console.warn('图片加载失败', err)
    imageLoadError.value = true
    currentImgIndex.value = safeIndex
  } finally {
    imageLoading.value = false
  }
}

const openPreview = async (imgArr, index = 0) => {
  if (!imgArr || !imgArr.length) {
    ElMessage.warning('暂无图片可预览')
    return
  }
  previewImageList.value = [...imgArr]
  currentImgIndex.value = index
  scale.value = 2.3
  imageLoadError.value = false
  showImageModal.value = true
  imageLoading.value = true
  try {
    await preloadImage(imgArr[index])
    preloadAdjacent(imgArr, index)
  } catch (e) {
    console.warn('初始图片加载失败', e)
    imageLoadError.value = true
  } finally {
    imageLoading.value = false
  }
}

const closeModal = () => {
  showImageModal.value = false
}

const prevImage = () => {
  if (imageLoading.value || previewImageList.value.length === 0) return
  switchToImage((currentImgIndex.value - 1 + previewImageList.value.length) % previewImageList.value.length)
}

const nextImage = () => {
  if (imageLoading.value || previewImageList.value.length === 0) return
  switchToImage((currentImgIndex.value + 1) % previewImageList.value.length)
}

const zoomIn = () => (scale.value += 0.2)
const zoomOut = () => (scale.value = Math.max(0.4, scale.value - 0.2))

const onImgError = (e) => {
  if (e && e.target && e.target.style) {
    e.target.style.visibility = 'hidden'
  }
}

onMounted(() => {
  getDailyList()
})
</script>

<template>
  <div class="article-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>日常管理</h1>
      <div class="header-buttons">
        <el-button v-perm="'sys:works:add'" type="primary" icon="Plus" @click="openAddDialog">发布日常</el-button>
        <el-button v-perm="'sys:works:delete'" type="danger" icon="Delete" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>
    <PermissionViewTip :perms="['sys:works:add','sys:works:edit','sys:works:delete']" />

    <!-- 查询条件区域 -->
    <el-card class="query-card" shadow="hover">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getDailyList">
        <el-form-item label="内容查询">
          <el-input v-model="queryForm.content" placeholder="请输入内容关键词" clearable style="width: 240px" />
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="置顶">
          <el-select v-model="queryForm.isTop" placeholder="全部" clearable style="width: 120px">
            <el-option label="已置顶" :value="1" />
            <el-option label="未置顶" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker
            v-model="queryForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getDailyList">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日常列表 -->
    <div class="list-card">
      <el-table
        :data="dailyList"
        v-loading="loading"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column prop="content" label="内容" min-width="200" align="center">
          <template #default="{ row }">
            <div class="wrap-title" :title="row.content">{{ row.content || '——' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getDailyTypeTag(row.type)">{{ getDailyTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="图片" width="180" align="center">
          <template #default="{ row }">
            <div v-if="row.images && row.images.split(',').filter(Boolean).length" class="image-carousel-wrapper">
              <el-carousel :interval="3000" trigger="click" height="110px" :autoplay="true" :pause-on-hover="true"
                indicator-position="none" arrow="always" loop>
                <el-carousel-item v-for="(img, idx) in row.images.split(',').filter(Boolean)" :key="idx">
                  <img :src="img" class="carousel-img" @error="onImgError" @click="openPreview(row.images.split(',').filter(Boolean), idx)" />
                </el-carousel-item>
              </el-carousel>
              <div class="image-count-badge">共{{ row.images.split(',').filter(Boolean).length }}张</div>
            </div>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column label="附件" width="160" align="center">
          <template #default="{ row }">
            <div v-if="row.files && row.files.split(',').filter(Boolean).length" class="file-list">
              <el-link
                v-for="(file, idx) in row.files.split(',').filter(Boolean).slice(0, 2)"
                :key="idx"
                type="primary"
                :href="file"
                target="_blank"
                class="file-link"
              >
                {{ file.split('/').pop() }}
              </el-link>
              <span v-if="row.files.split(',').filter(Boolean).length > 2" class="file-more">
                +{{ row.files.split(',').filter(Boolean).length - 2 }}
              </span>
            </div>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column label="是否置顶" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isTop === 1 ? 'success' : 'info'">
              {{ scope.row.isTop === 1 ? '已置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getDailyStatusType(scope.row.status)">
              {{ getDailyStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="200" align="center">
          <template #default="scope">
            <div class="time-group">
              <div>创建：{{ scope.row.createTime || '——' }}</div>
              <div v-if="scope.row.status === DAILY_STATUS.SCHEDULED">定时：{{ scope.row.timedPublishTime || '——' }}</div>
              <div v-else>发布：{{ scope.row.publishTime || '——' }}</div>
              <div>修改：{{ scope.row.updateTime || '——' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" align="center">
          <template #default="scope">
            <div class="action-buttons" v-loading="actionLoadingId === scope.row.id" element-loading-background="rgba(255,255,255,0.5)">
              <el-button type="primary" link :icon="View" @click="viewDaily(scope.row)">预览</el-button>

              <el-button v-perm="'sys:works:edit'" link :icon="Edit" @click="editDaily(scope.row)">修改</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="[DAILY_STATUS.DRAFT, DAILY_STATUS.SCHEDULED].includes(scope.row.status)"
                type="success"
                link
                :icon="Position"
                @click="changeStatus(scope.row, DAILY_STATUS.PUBLISHED)"
              >立即发布</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.PUBLISHED"
                type="primary"
                link
                :icon="Upload"
                @click="toggleTop(scope.row)"
              >{{ scope.row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="[DAILY_STATUS.PUBLISHED, DAILY_STATUS.SCHEDULED].includes(scope.row.status)"
                type="warning"
                link
                :icon="Hide"
                @click="togglePrivate(scope.row)"
              >设为私密</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.DRAFT"
                type="warning"
                link
                :icon="Timer"
                @click="openTimedDialog(scope.row.id)"
              >定时发布</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.SCHEDULED"
                type="warning"
                link
                :icon="Timer"
                @click="cancelTimed(scope.row.id)"
              >取消定时</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.PUBLISHED"
                type="danger"
                link
                :icon="Close"
                @click="changeStatus(scope.row, DAILY_STATUS.OFFLINE)"
              >下架</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.OFFLINE"
                type="success"
                link
                :icon="Check"
                @click="changeStatus(scope.row, DAILY_STATUS.PUBLISHED)"
              >上架</el-button>

              <el-button
                v-perm="'sys:works:edit'"
                v-if="scope.row.status === DAILY_STATUS.PRIVATE"
                type="warning"
                link
                :icon="View"
                @click="togglePrivate(scope.row)"
              >取消私密</el-button>

              <el-button v-perm="'sys:works:delete'" type="danger" link :icon="Delete" @click="deleteDaily(scope.row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty v-if="!loading && !loadError" description="暂无日常数据">
            <el-button type="primary" @click="getDailyList">刷新</el-button>
          </el-empty>
          <el-empty v-else-if="loadError" description="加载失败，请检查网络或稍后重试">
            <el-button type="primary" :icon="Refresh" @click="getDailyList">重新加载</el-button>
          </el-empty>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-box">
        <MyPagination
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @update:current-page="currentPage = $event; getDailyList()"
          @update:page-size="pageSize = $event; currentPage = 1; getDailyList()"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑日常' : '发布日常'"
      width="680px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="closeDialog"
      @close="submitLoading = false"
    >
      <el-form :model="contentForm" label-width="80px">
        <el-form-item label="内容描述" required>
          <el-input v-model="contentForm.content" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="请输入日常内容（必填）" />
        </el-form-item>

        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="imageFileList"
            list-type="picture-card"
            :http-request="customUpload"
            :on-success="handleUploadSuccess"
            :before-upload="beforeImageUpload"
            :multiple="true"
            accept="image/*"
          >
            <el-icon><Upload /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="上传附件">
          <el-upload
            v-model:file-list="fileFileList"
            :http-request="customUpload"
            :on-success="handleUploadSuccess"
            :before-upload="beforeFileUpload"
            :multiple="true"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="类型识别">
          <el-tag :type="getDailyTypeTag(currentType())">{{ getDailyTypeText(currentType()) }}</el-tag>
          <span class="type-tip">根据图片/附件自动识别，无需手动选择</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog" :disabled="submitLoading">取消</el-button>
        <template v-if="!isEdit">
          <el-button :loading="submitLoading" @click="submitForm(DAILY_STATUS.DRAFT)">存为草稿</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm(DAILY_STATUS.PUBLISHED)">立即发布</el-button>
        </template>
        <el-button v-else type="primary" :loading="submitLoading" @click="submitForm()">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 定时发布弹窗 -->
    <el-dialog v-model="timedDialogVisible" title="设置定时发布" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="timedPublishTime"
            type="datetime"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="timedDialogVisible = false" :disabled="timedSubmitLoading">取消</el-button>
        <el-button type="primary" :loading="timedSubmitLoading" @click="submitSetTimed">确认设置</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="日常预览" width="680px" top="8vh" :close-on-click-modal="true">
      <div class="preview-content">
        <div class="preview-meta">
          <span>类型：<el-tag size="small" :type="getDailyTypeTag(previewRow.type)">{{ getDailyTypeText(previewRow.type) }}</el-tag></span>
          <span>状态：<el-tag size="small" :type="getDailyStatusType(previewRow.status)">{{ getDailyStatusText(previewRow.status) }}</el-tag></span>
          <span>作者：{{ previewRow.userNickname || '——' }}</span>
          <span>创建：{{ previewRow.createTime || '——' }}</span>
        </div>

        <div class="preview-text">{{ previewRow.content || '暂无内容' }}</div>

        <div v-if="previewImages.length" class="preview-images">
          <div v-for="(img, idx) in previewImages" :key="idx" class="preview-img-item">
            <el-image :src="img" fit="cover" :preview-src-list="previewImages" :initial-index="idx" />
          </div>
        </div>

        <div v-if="previewFiles.length" class="preview-files">
          <div class="preview-files-title"><el-icon><Document /></el-icon> 附件</div>
          <el-link v-for="(file, idx) in previewFiles" :key="idx" type="primary" :href="file.url" target="_blank" class="preview-file-link">
            {{ file.name }}
          </el-link>
        </div>
      </div>
    </el-dialog>

    <!-- 大图预览 -->
    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <div class="img-close" @click="closeModal">✕</div>
      <div class="img-prev" @click="prevImage">‹</div>

      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="imageLoadError" class="image-loading-overlay">
        <el-icon :size="40" color="#fff"><WarningFilled /></el-icon>
        <span>图片加载失败</span>
      </div>

      <img
        v-else
        :src="previewImageList[currentImgIndex]"
        alt="预览"
        class="preview-image"
        :style="{ transform: `scale(${scale})` }"
        @click.stop
        draggable="false"
      />

      <div class="img-next" @click="nextImage">›</div>
      <div class="img-zoom">
        <div @click="zoomOut"><el-icon><ZoomOut /></el-icon></div>
        <div @click="zoomIn"><el-icon><ZoomIn /></el-icon></div>
        <div v-if="previewImageList.length" class="image-counter">{{ currentImgIndex + 1 }} / {{ previewImageList.length }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.article-management-container {
  padding: 0px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-buttons {
  display: flex;
  gap: 12px;
}

.query-card,
.list-card {
  margin-bottom: 20px;
}

.pagination-box {
  margin-top: 20px;
  text-align: right;
}

.wrap-title {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.time-group {
  font-size: 13px;
  font-weight: 450;
  line-height: 2.3;
  text-align: left;
  color: #393939;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 0px;
  padding: 4px 0;
  min-height: 56px;
  position: relative;
}

.action-buttons .el-button {
  width: 100%;
  margin: 0;
  justify-content: flex-start;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.type-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.file-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.file-link {
  display: inline-flex;
  max-width: 140px;
  line-height: 1.4;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.file-link :deep(.el-link__inner) {
  display: block;
  max-width: 100%;
  min-width: 0;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.file-more {
  font-size: 12px;
  color: #909399;
}

/* 预览弹窗 */
.preview-content {
  line-height: 1.8;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #6c757d;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 14px;
}

.preview-text {
  white-space: pre-wrap;
  word-break: break-word;
  color: #1d2129;
  margin-bottom: 16px;
}

.preview-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.preview-img-item {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.preview-img-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.preview-files {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-files-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #303133;
}

.preview-file-link {
  justify-content: flex-start;
}

/* 图片轮播容器 */
.image-carousel-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.carousel-img:hover {
  transform: scale(1.02);
}

.image-count-badge {
  position: absolute;
  bottom: 6px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 20px;
  z-index: 10;
  pointer-events: none;
  font-weight: 500;
}

:deep(.el-carousel__container) {
  height: 110px;
}

:deep(.el-carousel__arrow) {
  width: 24px;
  height: 24px;
  font-size: 14px;
  background-color: rgba(0, 0, 0, 0.4);
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.6);
}

:deep(.el-carousel__arrow--left) {
  left: 4px;
}

:deep(.el-carousel__arrow--right) {
  right: 4px;
}

/* ============= 大图预览 ============= */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.93);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  overflow: hidden !important;
}

.preview-image {
  max-width: 95%;
  max-height: 95vh;
  object-fit: contain;
  transition: transform 0.25s ease;
  transform-origin: center center;
}

.img-close {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 32px;
  color: #fff;
  cursor: pointer;
  z-index: 10002;
}

.img-prev,
.img-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 48px;
  color: #fff;
  cursor: pointer;
  user-select: none;
  background: rgba(0, 0, 0, 0.2);
  padding: 0 12px;
  border-radius: 4px;
  z-index: 10002;
}

.img-prev {
  left: 20px;
}

.img-next {
  right: 20px;
}

.img-zoom {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  align-items: center;
  z-index: 10002;
}

.img-zoom div:not(.image-counter) {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.img-zoom div:not(.image-counter):hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.05);
}

.image-counter {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  padding: 8px 14px;
  border-radius: 30px;
  font-size: 14px;
  font-family: monospace;
  color: white;
  margin-left: 8px;
  pointer-events: none;
  white-space: nowrap;
}

.image-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10001;
  color: white;
  font-size: 16px;
  gap: 12px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

:deep(.el-table__cell) {
  overflow-x: auto;
  padding: 0 8px;
}
</style>
