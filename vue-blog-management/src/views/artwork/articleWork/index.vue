<script setup>
import { ref, watch, reactive, onMounted } from 'vue'
import { useRouter } from "vue-router";
import MyPagination from '@/components/MyPagination.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete, Upload, Hide, Position, Timer,
  Close, FolderOpened, Check, FolderRemove, ZoomOut, ZoomIn
} from '@element-plus/icons-vue'
import { getStatusText, getStatusType, getStatusOptions } from '@/constants/articleConstants'
import { getCategoryOptionsApi } from '@/api/category.js'
import { getTagOptionsApi } from '@/api/tag.js'
import { getArticleListApi, setTimedApi, cancelTimedApi } from '@/api/article.js'
import { getAiChatApi } from '@/api/AIChat.js'

const router = useRouter();
const statusOptions = getStatusOptions();

// 查询条件
const queryForm = reactive({
  title: '',
  categoryId: '',
  tag: '',
  isTop: '',
  status: '',
  createTime: [],
  begin: '',
  end: ''
})

//侦听queryForm中的createTime属性
watch(() => queryForm.createTime, (newVal, oldVal) => {
  if (newVal.length == 2) {
    queryForm.begin = newVal[0];
    queryForm.end = newVal[1];
  } else {
    queryForm.begin = '';
    queryForm.end = '';
  }
})

// 文章列表
const articleList = ref([]);

// 模拟获取文章列表
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const testAiChat = async () => {
  const message = '你好'
  try {
    const res = await getAiChatApi(message)
    ElMessage.success(JSON.stringify(res.data))
  } catch (err) {
    ElMessage.error('AI调用失败：API Key无效，请后端配置正确密钥')
  }
}
const getArticleList = async () => {
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value
  };
  delete params.createTime;

  try {
    const result = await getArticleListApi(params)
    if (result.code == 200) {
      articleList.value = result.data.rows;
      total.value = result.data.total;
    } else {
      ElMessage.error(result.msg || '获取用户列表失败');
      articleList.value = [];
      total.value = 0;
    }

  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.title = ''
  queryForm.categoryId = ''
  queryForm.tag = ''
  queryForm.isTop = ''
  queryForm.status = ''
  queryForm.createTime = []
  queryForm.begin = ''
  queryForm.end = ''
  getArticleList()
}

//新增
const goto = (path) => {
  router.push(path)
}

// 提交发布
const submitArticle = () => {
  // 模拟提交
  ElMessage.success('文章发布成功！')
  getArticleList()
}

// 勾选复选框事件,复选框勾选发生变化时触发 val是当前选中的记录(数组)
const handleSelectionChange = (val) => {
  console.log(val)
}

// 查看
const viewArticle = (row) => {
  ElMessage.info(`查看：${row.title}`)
}
// 编辑
const editArticle = (row) => {
  router.push({
    path: '/editInput',
    query: { id: row.id, status: row.status }
  })
}
// 删除
const deleteArticle = (id) => {
  ElMessage.success(`删除文章 ID: ${id} 成功`)
  getArticleList()
}

const publishArticle = (id) => {
  ElMessage.success(`文章 ${id} 发布成功`)
  getArticleList()
}

const timedDialogVisible = ref(false)
const currentTimedId = ref(null)
const timedPublishTime = ref('')
// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}
const setTimed = (id) => {
  currentTimedId.value = id
  timedPublishTime.value = ''
  timedDialogVisible.value = true
}

// 提交定时设置
const submitSetTimed = async () => {
  if (!timedPublishTime.value) {
    ElMessage.warning('请选择定时时间')
    return
  }
  try {
    const tempData = {
      id: currentTimedId.value,
      timedPublishTime: timedPublishTime.value
    }
    const res = await setTimedApi(tempData)
    if (res.code === 200) {
      ElMessage.success('定时发布设置成功')
      timedDialogVisible.value = false
      getArticleList()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    ElMessage.error('设置失败，请重试')
  }
}

// 取消定时
const cancelTimed = (id) => {
  ElMessageBox.confirm('确定要取消定时发布吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '再想想',
    type: 'warning'
  }).then(async () => {
    const res = await cancelTimedApi(id)
    if (res.code === 200) {
      ElMessage.success('已取消定时发布')
      getArticleList()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(() => { })
}

const offlineArticle = (id) => {
  ElMessage.success(`文章 ${id} 已下架`)
  getArticleList()
}
const archiveArticle = (id) => {
  ElMessage.success(`文章 ${id} 已归档`)
  getArticleList()
}
// 私密独有
const setPrivate = (id) => {
  ElMessage.success(`文章 ${id} 已设为私密`)
  getArticleList()
}
const cancelPrivate = (id) => {
  ElMessage.success(`文章 ${id} 已取消私密`)
  getArticleList()
}

// 已下架独有
const onlineArticle = (id) => {
  ElMessage.success(`文章 ${id} 已上架`)
  getArticleList()
}

// 已归档独有
const cancelArchive = (id) => {
  ElMessage.success(`文章 ${id} 已取消归档`)
  getArticleList()
}


// 置顶切换
const toggleTop = (row) => {
  row.isTop = row.isTop === 1 ? 0 : 1
  ElMessage.success(row.isTop ? '已置顶' : '已取消置顶')
}

// 图片大图预览
const showImageModal = ref(false)
const previewImageUrl = ref('')
const scale = ref(2)

// 打开预览
const openPreview = (url) => {
  previewImageUrl.value = url
  showImageModal.value = true
  scale.value = 2
}

// 关闭预览
const closeModal = () => {
  showImageModal.value = false
}

// 缩放 + -
const zoomIn = () => scale.value += 0.2
const zoomOut = () => scale.value = Math.max(0.4, scale.value - 0.2)

// 上一张 / 下一张
const prevImage = () => { }
const nextImage = () => { }

// 分类列表
const categoryOptions = ref([])
// 获取分类下拉选项
const getCategoryOptions = async () => {
  try {
    const result = await getCategoryOptionsApi()
    if (result.code === 200) {
      categoryOptions.value = result.data
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败!');
  }
}

// 标签列表（从后端获取）
const tagOptions = ref([])

// 获取标签下拉选项
const getTagOptions = async () => {
  try {
    const result = await getTagOptionsApi()
    if (result.code === 200) {
      tagOptions.value = result.data
    }
  } catch (error) {
    ElMessage.error('获取标签列表失败!');
  }
}

// 根据标签 ID 获取名称
const getTagName = (tagId) => {
  if (!tagOptions.value || tagOptions.value.length === 0) return String(tagId)
  const found = tagOptions.value.find(item => String(item.value) === String(tagId))
  return found ? found.label : String(tagId)
}

onMounted(() => {
  getArticleList()
  getCategoryOptions()
  getTagOptions()
})
</script>


<template>
  <div class="article-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>文章管理</h1>
      <el-button type="primary" @click="goto('/editInput')">
        <el-icon>
          <Plus />
        </el-icon> 发布文章
      </el-button>
    </div>

    <!-- 查询条件区域 -->
    <el-card class="query-card" shadow="hover">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getArticleList">
        <el-form-item label="文章标题">
          <el-input v-model="queryForm.title" placeholder="请输入标题关键词" style="width: 360px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" style="width: 230px" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="queryForm.tag" placeholder="请输入标签" style="width: 230px" />
        </el-form-item>

        <el-form-item label="置顶">
          <el-select v-model="queryForm.isTop" placeholder="全部" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="已置顶" value="1" />
            <el-option label="未置顶" value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布时间">
          <el-date-picker v-model="queryForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" value-format="YYYY-MM-DD HH:mm:ss" />
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
          <el-button type="success" @click="testAiChat">AI单独测试</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文章列表 -->
    <div class="list-card">
      <!-- 表格 -->
      <el-table :data="articleList" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="封面" width="138" align="center">
          <template #default="scope">
            <div class="cover-box">
              <img :src="scope.row.cover" class="cover-img" alt="封面" @click="openPreview(scope.row.cover)" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="文章标题" min-width="160" align="center">
          <template #default="{ row }">
            <div class="wrap-title">
              <el-tooltip v-if="row.isHot" content="热门" placement="top">
                <font-awesome-icon icon="fire" class="hot-icon" />
              </el-tooltip>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="分类" width="70" align="center" />

        <el-table-column label="标签" width="105" align="center">
          <template #default="{ row }">
            <div class="tag-cell">
              <template v-if="row.tags && row.tags.length > 0">
                <el-tooltip placement="top" :show-after="200" popper-class="article-tags-tooltip">
                  <!-- 悬浮框内容：展示全部标签 -->
                  <template #content>
                    <div class="all-tags">
                      <el-tag v-for="(tagId, index) in row.tags" :key="index" size="small" type="primary">
                        {{ getTagName(tagId) }}
                      </el-tag>
                    </div>
                  </template>

                  <!-- 表格内显示：前2个标签 + 多余数量 -->
                  <div class="tag-show-list">
                    <el-tag v-for="(tagId, index) in row.tags.slice(0, 2)" :key="index" size="small" type="primary"
                      effect="light">
                      {{ getTagName(tagId) }}
                    </el-tag>
                    <span v-if="row.tags.length > 2" class="tag-more">
                      +{{ row.tags.length - 2 }}
                    </span>
                  </div>
                </el-tooltip>
              </template>
              <span v-else style="color:#aaa;font-size:13px;">——</span>
            </div>
          </template>
        </el-table-column>

        <!-- 置顶列 -->
        <el-table-column label="置顶" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isTop ? 'success' : 'info'">
              {{ scope.row.isTop ? '已置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="110" align="center">
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
              <div v-if="scope.row.status === 4">
                定时：{{ scope.row.timedPublishTime || '——' }}
              </div>
              <div v-else>发布：{{ scope.row.publishTime || '——' }}</div>
              <div>修改：{{ scope.row.updateTime || '——' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="230" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" link :icon="View" @click="viewArticle(scope.row)">预览</el-button>
              <el-button link :icon="Edit" @click="editArticle(scope.row)">修改</el-button>

              <el-button v-if="[0, 4].includes(scope.row.status)" type="success" link :icon="Position"
                @click="publishArticle(scope.row.id)">立即发布</el-button>

              <el-button v-if="[1, 3].includes(scope.row.status)" type="primary" link :icon="Upload"
                @click="toggleTop(scope.row)">
                {{ scope.row.isTop ? '取消置顶' : '置顶' }}
              </el-button>

              <el-button v-if="[1, 3].includes(scope.row.status)" type="warning" link :icon="Hide"
                @click="setPrivate(scope.row.id)">设为私密</el-button>


              <el-button v-if="scope.row.status === 0" type="warning" link :icon="Timer"
                @click="setTimed(scope.row.id)">定时发布</el-button>

              <el-button v-if="scope.row.status === 1" type="danger" link :icon="Close"
                @click="offlineArticle(scope.row.id)">下架</el-button>
              <el-button v-if="scope.row.status === 1" type="info" link :icon="FolderOpened"
                @click="archiveArticle(scope.row.id)">归档</el-button>


              <el-button v-if="scope.row.status === 2" type="success" link :icon="Check"
                @click="onlineArticle(scope.row.id)">上架</el-button>

              <el-button v-if="scope.row.status === 3" type="info" link :icon="FolderRemove"
                @click="cancelArchive(scope.row.id)">取消归档</el-button>

              <el-button v-if="scope.row.status === 4" type="warning" link :icon="Timer"
                @click="cancelTimed(scope.row.id)">取消定时</el-button>

              <el-button v-if="scope.row.status === 5" type="warning" link :icon="View"
                @click="cancelPrivate(scope.row.id)">取消私密</el-button>

              <el-button type="danger" link :icon="Delete" @click="deleteArticle(scope.row.id)">删除</el-button>
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

    <div v-if="showImageModal" class="image-modal" @click.self="closeModal">
      <!-- 关闭按钮 -->
      <div class="img-close" @click="closeModal">✕</div>

      <!-- 上一张 -->
      <div class="img-prev" @click="prevImage">‹</div>

      <!-- 图片 -->
      <img :src="previewImageUrl" alt="预览" class="preview-image" :style="{ transform: `scale(${scale})` }" @click.stop
        draggable="false" user-select="none" />

      <!-- 下一张 -->
      <div class="img-next" @click="nextImage">›</div>

      <!-- 缩放按钮 -->
      <div class="img-zoom">
        <div @click="zoomOut"><el-icon>
            <ZoomOut />
          </el-icon></div>
        <div @click="zoomIn"><el-icon>
            <ZoomIn />
          </el-icon></div>
      </div>
    </div>

    <el-dialog v-model="timedDialogVisible" title="设置定时发布" width="400px">
      <el-form label-width="80px">
        <el-form-item label="发布时间">
          <el-date-picker v-model="timedPublishTime" type="datetime" placeholder="选择发布时间"
            value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="timedDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSetTimed">确认设置</el-button>
      </template>
    </el-dialog>
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

.cover-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.wrap-title {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.hot-icon {
  color: #ff831e;
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

/* 图片大图预览样式 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.preview-image {
  max-width: 90%;
  max-height: 90%;
  transition: transform 0.2s;
}

.img-close {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 30px;
  color: white;
  cursor: pointer;
}

.img-prev,
.img-next {
  position: absolute;
  top: 50%;
  font-size: 40px;
  color: white;
  cursor: pointer;
  user-select: none;
}

.img-prev {
  left: 30px;
}

.img-next {
  right: 30px;
}

.img-zoom {
  position: absolute;
  bottom: 30px;
  display: flex;
  gap: 20px;
}

.img-zoom div {
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 10px;
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
</style>

<style>
/* 不带 scoped，全局有效 */
.article-tags-tooltip {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 8px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1) !important;
  padding: 10px 12px !important;
}

.article-tags-tooltip .all-tags {
  max-width: 320px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* 修改箭头的边框颜色（灰色） */
.article-tags-tooltip .el-popper__arrow {
  border-color: #dcdfe6 !important;
}

/* 修改箭头的背景颜色（白色）—— 箭头通常用伪元素实现背景 */
.article-tags-tooltip .el-popper__arrow::before {
  background: #ffffff !important;
  border-color: #dcdfe6 !important;
}
</style>