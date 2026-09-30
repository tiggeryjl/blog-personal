<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import MyPagination from '@/components/MyPagination.vue';
import PermissionViewTip from '@/components/PermissionViewTip.vue';
import { Search, Refresh } from '@element-plus/icons-vue';
import { getRecycleArticleListApi, recoverArticleApi, recycleDeleteArticleApi } from '@/api/article';

const loading = ref(false);
const articleList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const queryForm = reactive({
  title: '',
  status: '',
});

// 获取回收站文章列表
const getArticleList = async () => {
  loading.value = true;
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value,
  };
  try {
    const result = await getRecycleArticleListApi(params);
    if (result.code == 200) {
      articleList.value = result.data.rows || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result.msg || '获取回收站列表失败');
      articleList.value = [];
      total.value = 0;
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
    articleList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 查询
const handleSearch = () => {
  currentPage.value = 1;
  getArticleList();
};

// 重置搜索
const resetQuery = () => {
  queryForm.title = '';
  queryForm.status = '';
  currentPage.value = 1;
  getArticleList();
};

// 文章状态
const getStatusText = (status) => {
  const map = {
    0: '草稿',
    1: '已发布',
    2: '已下架',
    3: '已归档',
    4: '定时发布',
    5: '私密',
  };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning',
    4: 'primary',
    5: 'info',
  };
  return map[status] || '';
};

// 恢复单个文章
const handleRecover = (row) => {
  ElMessageBox.confirm('确认恢复该文章？恢复后将重新出现在文章管理中。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await recoverArticleApi([row.id]);
      if (result.code == 200) {
        ElMessage.success('恢复成功');
        if (articleList.value.length === 1 && currentPage.value > 1) {
          currentPage.value -= 1;
        }
        getArticleList();
      } else {
        ElMessage.error(result.msg || '恢复失败');
      }
    })
    .catch(() => {});
};

// 批量恢复
const selectedIds = ref([]);
const handleSelectionChange = (val) => {
  selectedIds.value = val.map((item) => item.id);
};

const handleBatchRecover = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要恢复的文章');
    return;
  }
  ElMessageBox.confirm(`确认恢复选中的 ${selectedIds.value.length} 篇文章？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await recoverArticleApi(selectedIds.value);
      if (result.code == 200) {
        ElMessage.success('批量恢复成功');
        if (articleList.value.length === selectedIds.value.length && currentPage.value > 1) {
          currentPage.value -= 1;
        }
        selectedIds.value = [];
        getArticleList();
      } else {
        ElMessage.error(result.msg || '批量恢复失败');
      }
    })
    .catch(() => {});
};

// 彻底删除单个文章
const handleDelete = (row) => {
  ElMessageBox.confirm('确认彻底删除该文章？删除后数据不可恢复！', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error',
  })
    .then(async () => {
      const result = await recycleDeleteArticleApi([row.id]);
      if (result.code == 200) {
        ElMessage.success('删除成功');
        if (articleList.value.length === 1 && currentPage.value > 1) {
          currentPage.value -= 1;
        }
        getArticleList();
      } else {
        ElMessage.error(result.msg || '删除失败');
      }
    })
    .catch(() => {});
};

// 批量彻底删除
const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要删除的文章');
    return;
  }
  ElMessageBox.confirm(`确认彻底删除选中的 ${selectedIds.value.length} 篇文章？删除后数据不可恢复！`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error',
  })
    .then(async () => {
      const result = await recycleDeleteArticleApi(selectedIds.value);
      if (result.code == 200) {
        ElMessage.success('批量删除成功');
        if (articleList.value.length === selectedIds.value.length && currentPage.value > 1) {
          currentPage.value -= 1;
        }
        selectedIds.value = [];
        getArticleList();
      } else {
        ElMessage.error(result.msg || '批量删除失败');
      }
    })
    .catch(() => {});
};

onMounted(() => {
  getArticleList();
});
</script>

<template>
  <div style="padding: 20px; max-width: 100%; overflow-x: hidden">
    <div
      class="header-row"
      style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px"
    >
      <h1 style="margin: 0">文章回收站</h1>
      <div>
        <el-button
          type="success"
          icon="RefreshLeft"
          v-perm="'sys:recycleArticleWork:recycle'"
          @click="handleBatchRecover"
          style="margin-right: 10px"
          >批量恢复</el-button
        >
        <el-button type="danger" icon="Delete" v-perm="'sys:recycleArticleWork:delete'" @click="handleBatchDelete"
          >批量彻底删除</el-button
        >
      </div>
    </div>
    <PermissionViewTip :perms="['sys:recycleArticleWork:recycle', 'sys:recycleArticleWork:delete']" />

    <!-- 搜索 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="queryForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" placeholder="请输入文章标题" style="width: 220px" clearable />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width: 150px" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
            <el-option label="已归档" :value="3" />
            <el-option label="定时发布" :value="4" />
            <el-option label="私密" :value="5" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
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
        :data="articleList"
        stripe
        v-loading="loading"
        style="width: 100%"
        :cell-style="{ borderRight: 'none' }"
        :header-cell-style="{ borderRight: 'none', textAlign: 'center' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />

        <el-table-column label="文章" min-width="240">
          <template #default="{ row }">
            <div style="line-height: 1.6">
              <div style="font-weight: 500">{{ row.title || '-' }}</div>
              <div style="font-size: 13px; color: #909399">{{ row.summary || '暂无摘要' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.category || '-' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="作者" width="160" align="center">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center; gap: 6px">
              <el-avatar :src="row.userAvatar" size="small">{{ row.userNickname?.charAt(0) }}</el-avatar>
              <span>{{ row.userNickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="更新时间" prop="updateTime" width="160" align="center" />

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link v-perm="'sys:recycleArticleWork:recycle'" @click="handleRecover(row)"
              >恢复</el-button
            >
            <el-button type="danger" link v-perm="'sys:recycleArticleWork:delete'" @click="handleDelete(row)"
              >彻底删除</el-button
            >
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
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @change="getArticleList()"
      />
    </div>
  </div>
</template>

<style scoped></style>
