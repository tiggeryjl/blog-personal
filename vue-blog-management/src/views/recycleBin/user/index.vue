<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MyPagination from '@/components/MyPagination.vue'
import { UserFilled, Search, Refresh } from '@element-plus/icons-vue'
import { getLogicDeleteUserApi, recoverUserApi, deleteUserApi } from '@/api/admin'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const queryForm = reactive({
  nickname: '',
  search: '',
  status: ''
})

// 获取逻辑删除的用户列表
const getUserList = async () => {
  loading.value = true
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value
  }
  try {
    const result = await getLogicDeleteUserApi(params)
    if (result.code == 200) {
      userList.value = result.data.rows || []
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.msg || '获取回收站列表失败')
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetQuery = () => {
  queryForm.nickname = ''
  queryForm.search = ''
  queryForm.status = ''
  currentPage.value = 1
  getUserList()
}

// 账号状态展示
const getStatusText = (status) => {
  if (status === 1) return '正常'
  if (status === 0) return '禁用'
  return '注销'
}

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 0) return 'danger'
  return 'info'
}

// 恢复用户
const handleRecover = (row) => {
  ElMessageBox.confirm('确认恢复该用户？恢复后用户将重新可用。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await recoverUserApi(row.id)
    if (result.code == 200) {
      ElMessage.success('恢复成功')
      getUserList()
    } else {
      ElMessage.error(result.msg || '恢复失败')
    }
  }).catch(() => { })
}

// 彻底删除单个用户
const handleDelete = (row) => {
  ElMessageBox.confirm('确认彻底删除该用户？删除后数据不可恢复！', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await deleteUserApi([row.id])
    if (result.code == 200) {
      ElMessage.success('删除成功')
      if (userList.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
      }
      getUserList()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  }).catch(() => { })
}

// 批量彻底删除
const selectedUsers = ref([])
const handleSelectionChange = (val) => {
  selectedUsers.value = val.map((item) => item.id)
}

const handleBatchDelete = () => {
  if (!selectedUsers.value || selectedUsers.value.length <= 0) {
    ElMessage.warning('请先勾选至少一个用户数据')
    return
  }
  ElMessageBox.confirm(`确认彻底删除选中的 ${selectedUsers.value.length} 个用户？删除后数据不可恢复！`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const result = await deleteUserApi(selectedUsers.value)
    if (result.code == 200) {
      ElMessage.success('批量删除成功')
      selectedUsers.value = []
      getUserList()
    } else {
      ElMessage.error(result.msg || '批量删除失败')
    }
  }).catch(() => { })
}

onMounted(() => {
  getUserList()
})
</script>

<template>
  <div class="user-recycle-container" style="padding: 20px; max-width: 100%; overflow-x: hidden">
    <div class="header-row" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
      <h1 style="margin: 0">用户回收站</h1>
      <el-button
        type="danger"
        icon="Delete"
        v-perm="'sys:recycleUser:delete'"
        @click="handleBatchDelete"
      >批量彻底删除</el-button>
    </div>

    <!-- 搜索 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getUserList">
        <el-form-item label="昵称">
          <el-input
            v-model="queryForm.nickname"
            placeholder="请输入昵称"
            style="width: 200px"
            clearable
          />
        </el-form-item>

        <el-form-item label="账号搜索">
          <el-input
            v-model="queryForm.search"
            placeholder="请输入用户名/手机号/邮箱"
            style="width: 220px"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 150px" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
            <el-option label="注销" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getUserList">
            <el-icon>
              <Search />
            </el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card>
      <el-table
        :data="userList"
        stripe
        v-loading="loading"
        style="width: 100%"
        :cell-style="{ borderRight: 'none' }"
        :header-cell-style="{ borderRight: 'none', textAlign: 'center' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="30" align="center" />

        <el-table-column label="头像" width="110" align="center">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; align-items: center; gap: 6px">
              <el-avatar :src="row.avatar" :icon="UserFilled" size="medium" />
              <span style="font-size: 13px">{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="账号信息" min-width="200">
          <template #default="{ row }">
            <div style="line-height: 1.6">
              <div>用户：{{ row.username }}</div>
              <div>手机：{{ row.phone || '-' }}</div>
              <div>邮箱：{{ row.email || '-' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="修改时间" prop="updateTime" width="160" align="center" />

        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button
              type="success"
              link
              v-perm="'sys:recycleUser:recycle'"
              @click="handleRecover(row)"
            >恢复</el-button>
            <el-button
              type="danger"
              link
              v-perm="'sys:recycleUser:delete'"
              @click="handleDelete(row)"
            >彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-box">
      <MyPagination
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @update:current-page="
          currentPage = $event;
          getUserList();
        "
        @update:page-size="
          pageSize = $event;
          getUserList();
        "
      />
    </div>
  </div>
</template>

<style scoped></style>
