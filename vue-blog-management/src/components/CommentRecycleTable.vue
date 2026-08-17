<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MyPagination from '@/components/MyPagination.vue'
import PermissionViewTip from '@/components/PermissionViewTip.vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  recoverCommentApi,
  recycleDeleteCommentApi
} from '@/api/comment'
import {
  getCommentStatusText,
  getCommentStatusType,
  getMessageTypeText
} from '@/constants/commentConstants'

/**
 * 评论回收站通用组件
 * listApi：当前类型回收站列表接口（type 由接口内部固定）
 */
const props = defineProps({
  pageTitle: { type: String, required: true },
  listApi: { type: Function, required: true },
  recyclePerm: { type: String, required: true },
  deletePerm: { type: String, required: true },
  showMsgType: { type: Boolean, default: false }
})

const loading = ref(false)
const commentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const queryForm = reactive({
  keyword: '',
  status: ''
})

// 获取回收站评论列表
const getCommentList = async () => {
  loading.value = true
  const params = {
    keyword: queryForm.keyword,
    status: queryForm.status,
    page: currentPage.value,
    pageSize: pageSize.value
  }
  try {
    const result = await props.listApi(params)
    if (result.code == 200) {
      commentList.value = result.data.rows || []
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.msg || '获取回收站列表失败')
      commentList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试')
    commentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetQuery = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  currentPage.value = 1
  getCommentList()
}

// 恢复单个评论
const handleRecover = (row) => {
  ElMessageBox.confirm('确认恢复该评论？恢复后其下回复将一并恢复。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await recoverCommentApi([row.id])
    if (result.code == 200) {
      ElMessage.success('恢复成功')
      getCommentList()
    } else {
      ElMessage.error(result.msg || '恢复失败')
    }
  }).catch(() => {})
}

// 批量恢复
const selectedIds = ref([])
const handleSelectionChange = (val) => {
  selectedIds.value = val.map((item) => item.id)
}

const handleBatchRecover = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要恢复的评论')
    return
  }
  ElMessageBox.confirm(`确认恢复选中的 ${selectedIds.value.length} 条评论？其下回复将一并恢复。`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await recoverCommentApi(selectedIds.value)
    if (result.code == 200) {
      ElMessage.success('批量恢复成功')
      selectedIds.value = []
      getCommentList()
    } else {
      ElMessage.error(result.msg || '批量恢复失败')
    }
  }).catch(() => {})
}

// 彻底删除单个评论
const handleDelete = (row) => {
  ElMessageBox.confirm('确认彻底删除该评论？其下回复将一并删除且不可恢复！', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await recycleDeleteCommentApi([row.id])
    if (result.code == 200) {
      ElMessage.success('删除成功')
      if (commentList.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
      }
      getCommentList()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  }).catch(() => {})
}

// 批量彻底删除
const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要删除的评论')
    return
  }
  ElMessageBox.confirm(`确认彻底删除选中的 ${selectedIds.value.length} 条评论？其下回复将一并删除且不可恢复！`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await recycleDeleteCommentApi(selectedIds.value)
    if (result.code == 200) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      getCommentList()
    } else {
      ElMessage.error(result.msg || '批量删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getCommentList()
})
</script>

<template>
  <div style="padding: 20px; max-width: 100%; overflow-x: hidden">
    <div class="header-row" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
      <h1 style="margin: 0">{{ pageTitle }}</h1>
      <div>
        <el-button
          type="success"
          icon="RefreshLeft"
          v-perm="recyclePerm"
          @click="handleBatchRecover"
          style="margin-right: 10px"
        >批量恢复</el-button>
        <el-button
          type="danger"
          icon="Delete"
          v-perm="deletePerm"
          @click="handleBatchDelete"
        >批量彻底删除</el-button>
      </div>
    </div>
    <PermissionViewTip :perms="[recyclePerm, deletePerm]" />

    <!-- 搜索 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getCommentList">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="评论内容/评论人昵称"
            style="width: 220px"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 130px" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="已隐藏" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getCommentList">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card>
      <el-table
        :data="commentList"
        stripe
        v-loading="loading"
        style="width: 100%"
        :cell-style="{ borderRight: 'none' }"
        :header-cell-style="{ borderRight: 'none', textAlign: 'center' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />

        <el-table-column label="评论人" width="180" align="center">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center; gap: 8px">
              <el-avatar :size="34" :src="row.userAvatar">
                {{ row.userNickname ? row.userNickname.charAt(0) : '' }}
              </el-avatar>
              <div style="text-align: left">
                <div>{{ row.userNickname || '-' }}</div>
                <div style="font-size: 12px; color: #909399">ID: {{ row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="240">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-text">{{ row.content || '-' }}</div>
              <el-tag
                v-if="row.parentId && row.parentId !== 0"
                size="small"
                type="info"
                style="margin-top: 6px"
              >
                回复 @{{ row.replyUserNickname || row.parentNickname || '-' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="来源" min-width="170" align="center">
          <template #default="{ row }">
            <div style="line-height: 1.5">
              <div>{{ row.sourceTitle || '-' }}</div>
              <div style="font-size: 12px; color: #909399">来源ID: {{ row.sourceId }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column v-if="showMsgType" label="留言类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ getMessageTypeText(row.msgType) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getCommentStatusType(row.status)">
              {{ getCommentStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评论时间" width="160" align="center" />

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              link
              v-perm="recyclePerm"
              @click="handleRecover(row)"
            >恢复</el-button>
            <el-button
              type="danger"
              link
              v-perm="deletePerm"
              @click="handleDelete(row)"
            >彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-box" style="margin-top: 20px">
      <MyPagination
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @update:current-page="currentPage = $event; getCommentList()"
        @update:page-size="pageSize = $event; getCommentList()"
      />
    </div>
  </div>
</template>

<style scoped>
.content-cell {
  text-align: left;
}

.content-text {
  word-break: break-all;
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>
