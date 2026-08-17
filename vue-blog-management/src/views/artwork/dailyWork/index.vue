<script setup>
import { ref, reactive, onMounted } from 'vue'
import MyPagination from '@/components/MyPagination.vue'
import PermissionViewTip from '@/components/PermissionViewTip.vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete,
  Upload, Hide, Position, Timer, Close, Check, ZoomIn, ZoomOut
} from '@element-plus/icons-vue'


// 查询条件
const queryForm = reactive({
  content: '',
  type: '',
  isTop: '',
  status: '',
  createTime: []
})

// 内容列表
const contentList = ref([])

// 弹窗 & 表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const contentForm = reactive({
  id: '',
  content: '',
  images: [],
  files: []
})

// 分页
const total = ref(51)
const currentPage = ref(1)
const pageSize = ref(10)

// ======================
// 【模拟数据 - 完整可显示】
// ======================
const getArticleList = () => {
  contentList.value = [
    {
      id: 1,
      content: '今天学习了Vue3+ElementPlus，完成了日常管理页面的开发，支持图片上传、文件管理、状态切换。',
      images: ['https://picsum.photos/200/200'],
      files: [{ name: '学习笔记.pdf', url: '#' }],
      status: 1,
      isTop: 1,
      createTime: '2025-05-25 10:20:00',
      publishTime: '2025-05-25 11:00:00',
      updateTime: '2025-05-25 14:30:00'
    },
    {
      id: 2,
      content: '分享一段生活日常：早上跑步、中午工作、晚上学习，充实的一天！',
      images: ['https://picsum.photos/201/201', 'https://picsum.photos/202/202', 'https://picsum.photos/202/203'],
      files: [],
      status: 1,
      isTop: 0,
      createTime: '2025-05-25 09:10:00',
      publishTime: '2025-05-25 09:30:00',
      updateTime: '2025-05-25 10:00:00'
    },
    {
      id: 3,
      content: '准备发布一篇技术文章，还没写完。',
      images: [],
      files: [{ name: '草稿资料.docx', url: '#' }],
      status: 0,
      isTop: 0,
      createTime: '2025-05-24 16:40:00',
      publishTime: '——',
      updateTime: '2025-05-24 17:00:00'
    },
    {
      id: 4,
      content: '这篇日常已下架，不再展示给用户。',
      images: ['https://picsum.photos/203/203'],
      files: [],
      status: 2,
      isTop: 0,
      createTime: '2025-05-23 20:18:00',
      publishTime: '2025-05-23 20:20:00',
      updateTime: '2025-05-23 21:00:00'
    },
    {
      id: 5,
      content: '这是私密内容，仅自己可见。',
      images: [],
      files: [],
      status: 4,
      isTop: 0,
      createTime: '2025-05-22 12:00:00',
      publishTime: '2025-05-22 12:00:00',
      updateTime: '2025-05-22 12:00:00'
    },
    {
      id: 6,
      content: '定时发布的日常，等待自动发布。',
      images: [],
      files: [],
      status: 3,
      isTop: 0,
      createTime: '2025-05-21 08:00:00',
      publishTime: '2025-05-26 00:00:00',
      updateTime: '2025-05-21 08:00:00'
    }
  ]
}

// 重置查询
const resetQuery = () => {
  queryForm.content = ''
  queryForm.type = ''
  queryForm.isTop = ''
  queryForm.status = ''
  queryForm.createTime = []
  getArticleList()
}

// 提交弹窗表单（修复缺失）
const submitForm = () => {
  ElMessage.success(isEdit ? '编辑成功' : '新增成功')
  dialogVisible.value = false
  getArticleList()
}

// 勾选复选框
const handleSelectionChange = (val) => {
  console.log('选中项：', val)
}

// 查看
const viewArticle = (row) => {
  ElMessage.info(`预览：${row.content.slice(0, 20)}...`)
}

const editArticle = () => { }

// 删除
const deleteArticle = (id) => {
  ElMessage.success(`删除 ID: ${id} 成功`)
  getArticleList()
}

// 发布
const publishArticle = (id) => {
  ElMessage.success(`文章 ${id} 发布成功`)
  getArticleList()
}

// 定时
const setTimed = (id) => {
  ElMessage.success(`文章 ${id} 定时设置成功`)
  getArticleList()
}

// 下架
const offlineArticle = (id) => {
  ElMessage.success(`文章 ${id} 已下架`)
  getArticleList()
}

// 私密
const setPrivate = (id) => {
  ElMessage.success(`文章 ${id} 已设为私密`)
  getArticleList()
}
const cancelPrivate = (id) => {
  ElMessage.success(`文章 ${id} 已取消私密`)
  getArticleList()
}

// 上架
const onlineArticle = (id) => {
  ElMessage.success(`文章 ${id} 已上架`)
  getArticleList()
}

// 取消定时
const cancelTimed = (id) => {
  ElMessage.success(`文章 ${id} 已取消定时`)
  getArticleList()
}

// 状态选项
const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 },
  { label: '定时发布', value: 3 },
  { label: '私密', value: 4 }
]

// 状态文本
const getStatusText = (status) => {
  const map = {
    0: '草稿',
    1: '已发布',
    2: '已下架',
    3: '定时发布',
    4: '私密'
  }
  return map[status] || '未知'
}

// 状态颜色
const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning',
    4: 'primary',
  }
  return map[status] || ''
}

// 置顶切换
const toggleTop = (row) => {
  row.isTop = row.isTop === 1 ? 0 : 1
  ElMessage.success(row.isTop ? '已置顶' : '已取消置顶')
}

// ========== 图片预览 & 多图轮播（优化版，支持预加载，消除卡顿） ==========
const showImageModal = ref(false)
const previewImageList = ref([])
const currentImgIndex = ref(0)
const scale = ref(2.3)
const imageLoading = ref(false)   // 加载状态，用于显示loading和防止快速点击

// 预加载单张图片（返回Promise，方便等待）
const preloadImage = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(url)
    img.onerror = (err) => reject(err)
    img.src = url
  })
}

// 预加载当前索引的上一张和下一张（用于后台静默加载）
const preloadAdjacent = (list, currentIndex) => {
  if (!list || list.length === 0) return
  const prevIndex = currentIndex > 0 ? currentIndex - 1 : list.length - 1
  const nextIndex = currentIndex < list.length - 1 ? currentIndex + 1 : 0
  if (list[prevIndex]) preloadImage(list[prevIndex]).catch(() => { }) // 静默失败
  if (list[nextIndex]) preloadImage(list[nextIndex]).catch(() => { })
}

// 切换到指定索引的图片（带loading和预加载）
const switchToImage = async (newIndex) => {
  if (imageLoading.value) return // 防止连续点击
  const targetUrl = previewImageList.value[newIndex]
  if (!targetUrl) return

  imageLoading.value = true
  try {
    await preloadImage(targetUrl)      // 确保当前图片加载完成
    currentImgIndex.value = newIndex   // 切换显示
    // 预加载新的相邻图片，为下一步点击做准备
    preloadAdjacent(previewImageList.value, newIndex)
  } catch (err) {
    console.warn('图片加载失败', err)
    // 即使失败也切换到该索引，显示可能存在的占位图或错误提示
    currentImgIndex.value = newIndex
  } finally {
    imageLoading.value = false
  }
}

// 打开预览（预加载当前及相邻图片）
const openPreview = async (imgArr, index = 0) => {
  previewImageList.value = imgArr
  currentImgIndex.value = index
  scale.value = 2.3
  showImageModal.value = true
  imageLoading.value = true
  try {
    // 加载当前图片
    await preloadImage(imgArr[index])
    // 后台预加载相邻图片
    preloadAdjacent(imgArr, index)
  } catch (e) {
    console.warn('初始图片加载失败', e)
  } finally {
    imageLoading.value = false
  }
}

const closeModal = () => {
  showImageModal.value = false
}

// 上一张（循环）
const prevImage = () => {
  if (imageLoading.value) return
  const len = previewImageList.value.length
  if (len === 0) return
  const newIndex = (currentImgIndex.value - 1 + len) % len
  switchToImage(newIndex)
}

// 下一张（循环）
const nextImage = () => {
  if (imageLoading.value) return
  const len = previewImageList.value.length
  if (len === 0) return
  const newIndex = (currentImgIndex.value + 1) % len
  switchToImage(newIndex)
}

const zoomIn = () => scale.value += 0.2
const zoomOut = () => scale.value = Math.max(0.4, scale.value - 0.2)

onMounted(() => {
  getArticleList()
})
</script>

<template>
  <div class="article-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>日常管理</h1>
      <el-button v-perm="'sys:works:add'" type="primary" @click="goto('/editInput')">
        <el-icon>
          <Plus />
        </el-icon> 发布日常
      </el-button>
    </div>
    <PermissionViewTip :perms="['sys:works:add','sys:works:edit','sys:works:delete']" />

    <!-- 查询条件区域 -->
    <el-card class="query-card" shadow="hover">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getArticleList">
        <el-form-item label="内容查询">
          <el-input v-model="queryForm.content" placeholder="请输入内容" style="width: 260px" />
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="文字" value="0" />
            <el-option label="图片" value="1" />
            <el-option label="文件" value="2" />
            <el-option label="图文混合" value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="置顶">
          <el-select v-model="queryForm.isTop" placeholder="全部" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="已置顶" value="0" />
            <el-option label="未置顶" value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布时间">
          <el-date-picker v-model="queryForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getArticleList">
            <el-icon>
              <Search />
            </el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon>
              <Refresh />
            </el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日常列表 -->
    <div class="list-card">
      <!-- 表格 -->
      <el-table :data="contentList" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column prop="content" label="内容" min-width="180" align="center">
          <template #default="{ row }">
            <div class="wrap-title">
              {{ row.content }}
            </div>
          </template>
        </el-table-column>

        <!-- 图片缩略图 -->
        <el-table-column label="图片" width="180" align="center">
          <template #default="{ row }">
            <div v-if="row.images && row.images.length" class="image-carousel-wrapper">
              <el-carousel :interval="3000" trigger="click" height="110px" :autoplay="true" :pause-on-hover="true"
                indicator-position="none" arrow="always" loop>
                <el-carousel-item v-for="(img, idx) in row.images" :key="idx">
                  <img :src="img" class="carousel-img" @click="openPreview(row.images, idx)" />
                </el-carousel-item>
              </el-carousel>
              <div class="image-count-badge">共{{ row.images.length }}张</div>
            </div>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <!-- 附件 -->
        <el-table-column label="附件" width="140" align="center">
          <template #default="{ row }">
            <el-link v-for="file in row.files" :key="file.name" type="primary" :href="file.url" download>
              {{ file.name }}
            </el-link>
            <span v-if="!row.files.length">—</span>
          </template>
        </el-table-column>

        <!-- 置顶列 -->
        <el-table-column label="是否置顶" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isTop ? 'success' : 'info'">
              {{ scope.row.isTop ? '已置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="时间" width="200" align="center">
          <template #default="scope">
            <div class="time-group">
              <div>创建：{{ scope.row.createTime || '——' }}</div>
              <div>发布：{{ scope.row.publishTime || '——' }}</div>
              <div>修改：{{ scope.row.updateTime || '——' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" link :icon="View" @click="viewArticle(scope.row)">预览</el-button>
              <el-button v-perm="'sys:works:edit'" link :icon="Edit" @click="editArticle(scope.row)">修改</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="[0, 3].includes(scope.row.status)" type="success" link :icon="Position"
                @click="publishArticle(scope.row.id)">立即发布</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 1" type="primary" link :icon="Upload" @click="toggleTop(scope.row)">
                {{ scope.row.isTop ? '取消置顶' : '置顶' }}
              </el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 1" type="warning" link :icon="Hide"
                @click="setPrivate(scope.row.id)">设为私密</el-button>


              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 0" type="warning" link :icon="Timer"
                @click="setTimed(scope.row.id)">定时发布</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 1" type="danger" link :icon="Close"
                @click="offlineArticle(scope.row.id)">下架</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 2" type="success" link :icon="Check"
                @click="onlineArticle(scope.row.id)">上架</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 3" type="warning" link :icon="Timer"
                @click="cancelTimed(scope.row.id)">取消定时</el-button>

              <el-button v-perm="'sys:works:edit'" v-if="scope.row.status === 4" type="warning" link :icon="View"
                @click="cancelPrivate(scope.row.id)">取消私密</el-button>

              <el-button v-perm="'sys:works:delete'" type="danger" link :icon="Delete" @click="deleteArticle(scope.row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-box">
        <MyPagination :total="total" :current-page="currentPage" :page-size="pageSize"
          @update:current-page="currentPage = $event" @update:page-size="pageSize = $event" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑内容' : '新增内容'" width="700px">
      <el-form :model="contentForm" label-width="80px">
        <el-form-item label="内容描述">
          <el-input v-model="contentForm.content" type="textarea" rows="4" placeholder="请输入内容" />
        </el-form-item>

        <!-- 多图上传 -->
        <el-form-item label="上传图片">
          <el-upload v-model:file-list="contentForm.images" list-type="picture-card" action="/api/upload"
            :multiple="true">
            <el-icon>
              <Upload />
            </el-icon>
          </el-upload>
        </el-form-item>

        <!-- 文件上传 -->
        <el-form-item label="上传附件">
          <el-upload v-model:file-list="contentForm.files" action="/api/upload" :multiple="true">
            <el-button type="primary">
              <el-icon>
                <Upload />
              </el-icon> 选择文件
            </el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认提交</el-button>
      </template>
    </el-dialog>

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">

      <div class="img-close" @click="closeModal">✕</div>
      <div class="img-prev" @click="prevImage">‹</div>

      <div v-if="imageLoading" class="image-loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <img :src="previewImageList[currentImgIndex]" alt="预览" class="preview-image"
        :style="{ transform: `scale(${scale})` }" @click.stop draggable="false" />

      <div class="img-next" @click="nextImage">›</div>
      <div class="img-zoom">
        <div @click="zoomOut"><el-icon>
            <ZoomOut />
          </el-icon></div>
        <div @click="zoomIn"><el-icon>
            <ZoomIn />
          </el-icon></div>
        <!-- 图片计数器（循环时提示当前位置） -->
        <div class="image-counter" v-if="previewImageList.length">
          {{ currentImgIndex + 1 }} / {{ previewImageList.length }}
        </div>
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

.query-card,
.list-card {
  margin-bottom: 20px;
}

.pagination-box {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.wrap-title {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
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

:deep(.el-table .cell) {
  overflow-x: visible;
}

:deep(.el-table__row) {
  height: auto !important;
}

:deep(.el-table__cell) {
  overflow-x: auto;
  padding: 0 8px;
}

/* 图片轮播容器 */
.image-carousel-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

/* 轮播图图片样式 */
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

/* 图片数量角标 */
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

/* 覆盖 Element Plus Carousel 默认样式，使其适配小尺寸 */
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
}

.img-prev {
  left: 20px;
}

.img-next {
  right: 20px;
}

/* 缩放按钮 */
.img-zoom {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  align-items: center;
  z-index: 10;
}

/* 缩放按钮（圆形） */
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

/* 计数器样式 - 独立矩形，与按钮间隔开 */
.image-counter {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  padding: 8px 14px;
  border-radius: 30px;
  font-size: 14px;
  font-family: monospace;
  color: white;
  margin-left: 8px;
  /* 与缩放按钮拉开距离 */
  pointer-events: none;
  white-space: nowrap;
}

/* 图片加载遮罩 */
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
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 附件 */
:deep(.el-table__cell .el-link) {
  display: inline-block;
  text-align: center;
  font-size: 12px;
}
</style>
