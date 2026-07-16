<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from "vue-router";
import MyPagination from '@/components/MyPagination.vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete,
  Upload, Hide, Position, Timer, Close, FolderOpened,
  Check, FolderRemove, ZoomOut, ZoomIn
} from '@element-plus/icons-vue'

const router = useRouter();

// 查询条件
const queryForm = reactive({
  title: '',
  Category: '',
  Tag: '',
  isTop: '',
  status: '',
  createTime: []
})

// 文章列表
const articleList = ref([])

// 模拟获取文章列表
const total = ref(51)
const currentPage = ref(1)
const pageSize = ref(10)
const getArticleList = () => {
  console.log(queryForm)
  // 这里替换成你的接口请求
  articleList.value = [
    {
      id: 1,
      cover: 'https://picsum.photos/400/224',
      title: 'Vue3 + ElementPlus 后台管理实战',
      category: '技术',
      tags: 'Vue3,前端',
      status: 1,
      isTop: 1,
      isHot: 1,
      createTime: '2025-01-01 12:00:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    },
    {
      id: 2,
      cover: 'https://picsum.photos/400/224',
      title: '个人博客开发日记',
      category: '生活',
      tags: '博客,记录',
      status: 0,
      isTop: 0,
      isHot: 0,
      createTime: '2025-01-02 15:30:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    },
    {
      id: 3,
      cover: 'https://picsum.photos/400/224',
      title: '个人博客开发日记',
      category: '生活',
      tags: '博客,记录',
      status: 2,
      isTop: 0,
      isHot: 0,
      createTime: '2025-01-02 15:30:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    },
    {
      id: 4,
      cover: 'https://picsum.photos/400/224',
      title: '个人博客开发日记',
      category: '生活',
      tags: '博客,记录',
      status: 3,
      isTop: 1,
      isHot: 1,
      createTime: '2025-01-02 15:30:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    },
    {
      id: 5,
      cover: 'https://picsum.photos/400/224',
      title: '个人博客开发日记',
      category: '生活',
      tags: '博客,记录',
      status: 4,
      isTop: 0,
      isHot: 0,
      createTime: '2025-01-02 15:30:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    },
    {
      id: 6,
      cover: 'https://picsum.photos/400/224',
      title: '个人博客开发日记',
      category: '生活',
      tags: '博客,记录',
      status: 5,
      isTop: 0,
      isHot: 0,
      createTime: '2025-01-02 15:30:00',
      publishTime: '2025-01-01 12:00:00',
      updateTime: '2025-01-01 12:00:00'
    }
  ]
}

// 重置查询
const resetQuery = () => {
  queryForm.title = ''
  queryForm.Category = ''
  queryForm.Tag = ''
  queryForm.isTop = ''
  queryForm.status = ''
  queryForm.createTime = []
  getArticleList()
}

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
  router.push('/editInput')
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
const setTimed = (id) => {
  ElMessage.success(`文章 ${id} 定时设置成功`)
  getArticleList()
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

// 定时发布独有
const cancelTimed = (id) => {
  ElMessage.success(`文章 ${id} 已取消定时`)
  getArticleList()
}



// 文章状态列表（动态给下拉框用）
const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 },
  { label: '已归档', value: 3 },
  { label: '定时发布', value: 4 },
  { label: '私密', value: 5 }
]
// 状态文本映射
const getStatusText = (status) => {
  const map = {
    0: '草稿',
    1: '已发布',
    2: '已下架',
    3: '已归档',
    4: '定时发布',
    5: '私密'
  }
  return map[status] || '未知'
}

// 状态标签颜色
const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning',
    4: 'primary',
    5: 'primary'
  }
  return map[status] || ''
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

onMounted(() => {
  getArticleList()
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
          <el-input v-model="queryForm.Category" placeholder="请输入分类" style="width: 230px" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="queryForm.Tag" placeholder="请输入标签" style="width: 230px" />
        </el-form-item>

        <el-form-item label="置顶">
          <el-select v-model="queryForm.isTop" placeholder="全部" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="已置顶" value="0" />
            <el-option label="未置顶" value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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

        <el-table-column prop="title" label="文章标题" min-width="160">
          <template #default="{ row }">
            <div class="wrap-title">
              <el-tooltip v-if="row.isHot" content="热门" placement="top">
                <font-awesome-icon icon="fire" class="hot-icon" />
              </el-tooltip>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="分类" width="70" />
        <el-table-column prop="tags" label="标签" width="100" />

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
              <div>发布：{{ scope.row.publishTime || '——' }}</div>
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