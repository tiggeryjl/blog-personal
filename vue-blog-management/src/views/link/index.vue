<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Refresh, Delete } from '@element-plus/icons-vue';
import PermissionViewTip from '@/components/PermissionViewTip.vue';
import MyPagination from '@/components/MyPagination.vue';
import {
  getLinkPageApi,
  getLinkStatsApi,
  getLinkByIdApi,
  addLinkApi,
  updateLinkApi,
  auditLinkApi,
  updateLinkStatusApi,
  deleteLinkApi,
} from '@/api/link.js';
import {
  LINK_AUDIT_STATUS,
  getLinkAuditStatusText,
  getLinkAuditStatusType,
  getLinkStatusText,
  getLinkStatusType,
} from '@/constants/linkConstants.js';

// ===================== 查询条件 =====================
const queryForm = reactive({
  keyword: '',
  auditStatus: null,
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const list = ref([]);
const loading = ref(false);

// 审核状态统计
const stats = reactive({ total: 0, pending: 0, approved: 0, rejected: 0 });

const auditTabs = [
  { label: '全部', value: null, key: 'total', theme: 'primary' },
  { label: '待审核', value: LINK_AUDIT_STATUS.PENDING, key: 'pending', theme: 'warning' },
  { label: '已通过', value: LINK_AUDIT_STATUS.APPROVED, key: 'approved', theme: 'success' },
  { label: '已拒绝', value: LINK_AUDIT_STATUS.REJECTED, key: 'rejected', theme: 'danger' },
];

const getLinkList = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
    };
    if (queryForm.keyword) params.keyword = queryForm.keyword;
    if (queryForm.auditStatus !== null && queryForm.auditStatus !== '') {
      params.auditStatus = queryForm.auditStatus;
    }
    const result = await getLinkPageApi(params);
    if (result.code == 200) {
      list.value = result.data.rows || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result.msg || '获取友链列表失败');
    }
  } catch (e) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const getStats = async () => {
  const result = await getLinkStatsApi();
  if (result.code == 200) {
    Object.assign(stats, result.data || {});
  }
};

const switchTab = (value) => {
  queryForm.auditStatus = value;
  currentPage.value = 1;
  getLinkList();
};

const handleSearch = () => {
  currentPage.value = 1;
  getLinkList();
};

const handleReset = () => {
  queryForm.keyword = '';
  queryForm.auditStatus = null;
  currentPage.value = 1;
  getLinkList();
};

// ===================== 新增/编辑 =====================
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref(null);
const linkForm = reactive({
  id: null,
  linkName: '',
  linkUrl: '',
  linkAvatar: '',
  linkDesc: '',
  sort: 0,
  auditStatus: LINK_AUDIT_STATUS.APPROVED,
  status: 1,
});

const rules = {
  linkName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  linkUrl: [
    { required: true, message: '请输入网站链接', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/i, message: '请输入合法的网址（http/https）', trigger: 'blur' },
  ],
};

const resetForm = () => {
  Object.assign(linkForm, {
    id: null,
    linkName: '',
    linkUrl: '',
    linkAvatar: '',
    linkDesc: '',
    sort: 0,
    auditStatus: LINK_AUDIT_STATUS.APPROVED,
    status: 1,
  });
  formRef.value?.clearValidate();
};

const openAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = async (row) => {
  isEdit.value = true;
  resetForm();
  const result = await getLinkByIdApi(row.id);
  if (result.code == 200) {
    Object.assign(linkForm, result.data);
    dialogVisible.value = true;
  } else {
    ElMessage.error(result.msg || '查询友链失败');
  }
};

const saveLink = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    const result = isEdit.value ? await updateLinkApi(linkForm) : await addLinkApi(linkForm);
    if (result.code == 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      dialogVisible.value = false;
      getLinkList();
      getStats();
    } else {
      ElMessage.error(result.msg || '保存失败');
    }
  });
};

// ===================== 审核操作 =====================
const auditLink = (row, auditStatus) => {
  const actionText = auditStatus === LINK_AUDIT_STATUS.APPROVED ? '通过' : '拒绝';
  ElMessageBox.confirm(`确定${actionText}友链「${row.linkName}」吗？`, '审核提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await auditLinkApi({ ids: [row.id], auditStatus });
      if (result.code == 200) {
        ElMessage.success(`已${actionText}该友链`);
        getLinkList();
        getStats();
      } else {
        ElMessage.error(result.msg || '审核失败');
      }
    })
    .catch(() => {});
};

// ===================== 启用/禁用 =====================
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1;
  const result = await updateLinkStatusApi(row.id, newStatus);
  if (result.code == 200) {
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用');
    getLinkList();
  } else {
    ElMessage.error(result.msg || '状态修改失败');
  }
};

// ===================== 删除 =====================
const selectedIds = ref([]);

const handleSelectionChange = (val) => {
  selectedIds.value = val.map((item) => item.id);
};

const deleteLink = (ids) => {
  const count = Array.isArray(ids) ? ids.length : 1;
  ElMessageBox.confirm(`确定删除选中的 ${count} 条友链吗？删除后可在回收站恢复。`, '删除提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await deleteLinkApi(ids);
      if (result.code == 200) {
        ElMessage.success('删除成功');
        getLinkList();
        getStats();
      } else {
        ElMessage.error(result.msg || '删除失败');
      }
    })
    .catch(() => {});
};

const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择要删除的友链');
    return;
  }
  deleteLink(selectedIds.value);
};

const openLink = (url) => {
  if (url) window.open(url, '_blank');
};

onMounted(() => {
  getLinkList();
  getStats();
});
</script>

<template>
  <div class="main-container">
    <h1>友链管理</h1>
    <PermissionViewTip :perms="['sys:link:add', 'sys:link:edit', 'sys:link:delete']" />

    <!-- 审核状态统计 -->
    <div class="stats-row">
      <div
        v-for="tab in auditTabs"
        :key="tab.key"
        :class="['stat-card', `stat-card--${tab.theme}`, { active: (queryForm.auditStatus ?? null) === tab.value }]"
        @click="switchTab(tab.value)"
      >
        <div class="stat-num">{{ stats[tab.key] ?? 0 }}</div>
        <div class="stat-label">{{ tab.label }}</div>
      </div>
    </div>

    <!-- 查询区 -->
    <div class="filter-bar">
      <el-input
        v-model="queryForm.keyword"
        placeholder="搜索网站名称 / 链接"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-perm="'sys:link:add'" type="primary" :icon="Plus" @click="openAdd">新增友链</el-button>
      <el-button v-perm="'sys:link:delete'" type="danger" :icon="Delete" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 友链表 -->
    <el-table :data="list" border v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.linkAvatar"
            :src="row.linkAvatar"
            fit="cover"
            class="avatar-img"
            :preview-src-list="[row.linkAvatar]"
            preview-teleported
          >
            <template #error>
              <div class="avatar-fallback">{{ (row.linkName || '?').slice(0, 1) }}</div>
            </template>
          </el-image>
          <div v-else class="avatar-fallback">{{ (row.linkName || '?').slice(0, 1) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="网站名称" prop="linkName" min-width="130" align="center" show-overflow-tooltip />
      <el-table-column label="网站链接" min-width="180" align="center">
        <template #default="{ row }">
          <el-link type="primary" :href="row.linkUrl" target="_blank" :underline="false">
            {{ row.linkUrl }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="站点简介" prop="linkDesc" min-width="180" align="center" show-overflow-tooltip />
      <el-table-column label="排序" prop="sort" width="70" align="center" />
      <el-table-column label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getLinkAuditStatusType(row.auditStatus)">
            {{ getLinkAuditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="展示状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getLinkStatusType(row.status)">
            {{ getLinkStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.auditStatus !== LINK_AUDIT_STATUS.APPROVED"
            link
            v-perm="'sys:link:edit'"
            type="success"
            @click="auditLink(row, LINK_AUDIT_STATUS.APPROVED)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.auditStatus !== LINK_AUDIT_STATUS.REJECTED"
            link
            v-perm="'sys:link:edit'"
            type="danger"
            @click="auditLink(row, LINK_AUDIT_STATUS.REJECTED)"
          >
            拒绝
          </el-button>
          <el-button link v-perm="'sys:link:edit'" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.auditStatus === LINK_AUDIT_STATUS.APPROVED"
            link
            v-perm="'sys:link:edit'"
            type="warning"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link v-perm="'sys:link:delete'" type="danger" @click="deleteLink([row.id])">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无友链数据" />
      </template>
    </el-table>

    <!-- 分页 -->
    <MyPagination
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      @update:current-page="
        currentPage = $event;
        getLinkList();
      "
      @update:page-size="
        pageSize = $event;
        currentPage = 1;
        getLinkList();
      "
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑友链' : '新增友链'" width="560px" append-to-body>
      <el-form ref="formRef" :model="linkForm" :rules="rules" label-width="90px">
        <el-form-item label="网站名称" prop="linkName">
          <el-input v-model="linkForm.linkName" placeholder="请输入网站名称" />
        </el-form-item>
        <el-form-item label="网站链接" prop="linkUrl">
          <el-input v-model="linkForm.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="linkForm.linkAvatar" placeholder="图片 URL，可留空">
            <template #append>
              <el-button @click="openLink(linkForm.linkAvatar)">预览</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="站点简介">
          <el-input
            v-model="linkForm.linkDesc"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入站点简介"
          />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="linkForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-radio-group v-model="linkForm.auditStatus">
            <el-radio :value="LINK_AUDIT_STATUS.PENDING">待审核</el-radio>
            <el-radio :value="LINK_AUDIT_STATUS.APPROVED">已通过</el-radio>
            <el-radio :value="LINK_AUDIT_STATUS.REJECTED">已拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="展示状态">
          <el-radio-group v-model="linkForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLink">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.main-container {
  padding: 0 20px;
}

.main-container h1 {
  padding-bottom: 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 18px 20px 18px 24px;
  border: 1px solid var(--card-border);
  border-radius: 8px;
  background: var(--card-bg);
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
  /* 默认主题，避免缺少主题类时样式失效 */
  --card-color: #409eff;
  --card-bg: #ecf5ff;
  --card-border: #d9ecff;
  --card-shadow: rgba(64, 158, 255, 0.18);
}

.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--card-color);
}

.stat-card:hover {
  border-color: var(--card-color);
  box-shadow: 0 2px 10px var(--card-shadow);
}

.stat-card.active {
  border-color: var(--card-color);
  box-shadow: 0 2px 10px var(--card-shadow);
}

.stat-card--primary {
  --card-color: #409eff;
  --card-bg: #ecf5ff;
  --card-border: #d9ecff;
  --card-shadow: rgba(64, 158, 255, 0.18);
}

.stat-card--warning {
  --card-color: #e6a23c;
  --card-bg: #fdf6ec;
  --card-border: #f5dab1;
  --card-shadow: rgba(230, 162, 60, 0.18);
}

.stat-card--success {
  --card-color: #67c23a;
  --card-bg: #f0f9eb;
  --card-border: #c2e7b0;
  --card-shadow: rgba(103, 194, 58, 0.18);
}

.stat-card--danger {
  --card-color: #f56c6c;
  --card-bg: #fef0f0;
  --card-border: #fbc4c4;
  --card-shadow: rgba(245, 108, 108, 0.18);
}

.stat-num {
  font-size: 26px;
  font-weight: 600;
  color: var(--card-color);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.filter-bar .spacer {
  flex: 1;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.avatar-fallback {
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
