<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MyPagination from '@/components/MyPagination.vue'
import PermissionViewTip from '@/components/PermissionViewTip.vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getRecycleCategoryListApi,
  recoverCategoryApi,
  recycleDeleteCategoryApi
} from '@/api/category'
import {
  getRecycleTagListApi,
  recoverTagApi,
  recycleDeleteTagApi
} from '@/api/tag'

const activeTab = ref('category')
const loading = ref(false)
const categoryList = ref([])
const tagList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const tableRef = ref(null)

// 搜索表单
const queryForm = reactive({
  keyword: ''
})

// 当前激活标签对应的数据与接口
const tableData = computed(() =>
  activeTab.value === 'category' ? categoryList.value : tagList.value
)

const listApiMap = {
  category: getRecycleCategoryListApi,
  tag: getRecycleTagListApi
}
const recoverApiMap = {
  category: recoverCategoryApi,
  tag: recoverTagApi
}
const deleteApiMap = {
  category: recycleDeleteCategoryApi,
  tag: recycleDeleteTagApi
}

// 获取当前标签的回收站列表
const getList = async () => {
  loading.value = true
  const params = {
    keyword: queryForm.keyword,
    page: currentPage.value,
    pageSize: pageSize.value
  }
  try {
    const result = await listApiMap[activeTab.value](params)
    if (result.code == 200) {
      if (activeTab.value === 'category') {
        categoryList.value = result.data.rows || []
      } else {
        tagList.value = result.data.rows || []
      }
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.msg || '获取回收站列表失败')
      total.value = 0
      if (activeTab.value === 'category') {
        categoryList.value = []
      } else {
        tagList.value = []
      }
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试')
    total.value = 0
    if (activeTab.value === 'category') {
      categoryList.value = []
    } else {
      tagList.value = []
    }
  } finally {
    loading.value = false
  }
}

// 切换标签：重置分页并重新加载
const handleTabChange = () => {
  currentPage.value = 1
  selectedIds.value = []
  getList()
}

// 重置搜索
const resetQuery = () => {
  queryForm.keyword = ''
  currentPage.value = 1
  getList()
}

// 状态
const getStatusText = (status) => (status === 1 ? '启用' : '禁用')
const getStatusType = (status) => (status === 1 ? 'success' : 'danger')

// 恢复单个
const handleRecover = (row) => {
  ElMessageBox.confirm(`确认恢复该${activeTab.value === 'category' ? '分类' : '标签'}？恢复后将重新出现在管理中。`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await recoverApiMap[activeTab.value]([row.id])
    if (result.code == 200) {
      ElMessage.success('恢复成功')
      getList()
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
    ElMessage.warning('请先勾选要恢复的数据')
    return
  }
  ElMessageBox.confirm(`确认恢复选中的 ${selectedIds.value.length} 条数据？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await recoverApiMap[activeTab.value](selectedIds.value)
    if (result.code == 200) {
      ElMessage.success('批量恢复成功')
      selectedIds.value = []
      getList()
    } else {
      ElMessage.error(result.msg || '批量恢复失败')
    }
  }).catch(() => {})
}

// 彻底删除单个
const handleDelete = (row) => {
  ElMessageBox.confirm('确认彻底删除该数据？删除后数据不可恢复！', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await deleteApiMap[activeTab.value]([row.id])
    if (result.code == 200) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
      }
      getList()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  }).catch(() => {})
}

// 批量彻底删除
const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要删除的数据')
    return
  }
  ElMessageBox.confirm(`确认彻底删除选中的 ${selectedIds.value.length} 条数据？删除后数据不可恢复！`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await deleteApiMap[activeTab.value](selectedIds.value)
    if (result.code == 200) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      getList()
    } else {
      ElMessage.error(result.msg || '批量删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div style="padding: 20px; max-width: 100%; overflow-x: hidden">
    <div class="header-row" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
      <h1 style="margin: 0">分类标签回收站</h1>
      <div>
        <el-button
          type="success"
          icon="RefreshLeft"
          v-perm="'sys:recycleCategoryTag:recycle'"
          @click="handleBatchRecover"
          style="margin-right: 10px"
        >批量恢复</el-button>
        <el-button
          type="danger"
          icon="Delete"
          v-perm="'sys:recycleCategoryTag:delete'"
          @click="handleBatchDelete"
        >批量彻底删除</el-button>
      </div>
    </div>
    <PermissionViewTip :perms="['sys:recycleCategoryTag:recycle','sys:recycleCategoryTag:delete']" />

    <!-- 搜索 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getList">
        <el-form-item label="名称">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入名称关键词"
            style="width: 220px"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">
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
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="分类" name="category" />
        <el-tab-pane label="标签" name="tag" />
      </el-tabs>

      <el-table
        ref="tableRef"
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        :cell-style="{ borderRight: 'none' }"
        :header-cell-style="{ borderRight: 'none', textAlign: 'center' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />

        <el-table-column prop="name" label="名称" min-width="180" align="center" />

        <el-table-column v-if="activeTab === 'category'" prop="description" label="描述" min-width="220" align="center">
          <template #default="{ row }">
            <span>{{ row.description || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center" />

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              link
              v-perm="'sys:recycleCategoryTag:recycle'"
              @click="handleRecover(row)"
            >恢复</el-button>
            <el-button
              type="danger"
              link
              v-perm="'sys:recycleCategoryTag:delete'"
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
        @update:current-page="currentPage = $event; getList()"
        @update:page-size="pageSize = $event; getList()"
      />
    </div>
  </div>
</template>

<style scoped></style>
