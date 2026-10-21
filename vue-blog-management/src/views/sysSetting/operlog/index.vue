<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import MyPagination from '@/components/MyPagination.vue';
import PermissionViewTip from '@/components/PermissionViewTip.vue';
import { usePermissionStore } from '@/stores/permission';
import {
  getOperLogPageApi,
  getOperLogDetailApi,
  deleteOperLogApi,
  clearOperLogApi,
} from '@/api/operLog.js';
import {
  OPER_LOG_TYPE_OPTIONS,
  getOperLogTypeText,
  getOperLogTypeTagType,
  getOperLogStatusText,
  getOperLogStatusTagType,
} from '@/constants/operLogConstants.js';

const ROLE_KEY_SUPER = 'admin';
const RETENTION_DAYS = 90;

const permissionStore = usePermissionStore();
const isSuperAdmin = computed(() => (permissionStore.roles || []).includes(ROLE_KEY_SUPER));

// ===================== 查询条件 =====================
const queryForm = reactive({
  keyword: '',
  moduleName: '',
  operType: null,
  status: null,
  dateRange: [],
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const list = ref([]);
const loading = ref(false);
const selectedIds = ref([]);

// ===================== 详情弹窗 =====================
const detailVisible = ref(false);
const detail = ref({});

// ===================== 查询 =====================
const getList = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
    };
    if (queryForm.keyword.trim()) params.keyword = queryForm.keyword.trim();
    if (queryForm.moduleName.trim()) params.moduleName = queryForm.moduleName.trim();
    if (queryForm.operType !== null && queryForm.operType !== '') {
      params.operType = queryForm.operType;
    }
    if (queryForm.status !== null && queryForm.status !== '') {
      params.status = queryForm.status;
    }
    if (Array.isArray(queryForm.dateRange) && queryForm.dateRange.length === 2) {
      params.beginTime = queryForm.dateRange[0];
      params.endTime = queryForm.dateRange[1];
    }

    const result = await getOperLogPageApi(params);
    if (result.code === 200) {
      list.value = result.data.rows || [];
      total.value = Number(result.data.total) || 0;
    } else {
      ElMessage.error(result.msg || '获取操作日志失败');
    }
  } catch (e) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getList();
};

const handleReset = () => {
  queryForm.keyword = '';
  queryForm.moduleName = '';
  queryForm.operType = null;
  queryForm.status = null;
  queryForm.dateRange = [];
  currentPage.value = 1;
  getList();
};

const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((item) => item.id);
};

// ===================== 详情 =====================
const openDetail = async (row) => {
  try {
    const result = await getOperLogDetailApi(row.id);
    if (result.code === 200) {
      detail.value = result.data || {};
      detailVisible.value = true;
    } else {
      ElMessage.error(result.msg || '获取日志详情失败');
    }
  } catch (e) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
};

const prettyJson = (text) => {
  if (!text) return '—';
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch (e) {
    return text;
  }
};

// ===================== 删除 =====================
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedIds.value.length} 条日志吗？删除后无法恢复。`,
      '删除日志',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  const result = await deleteOperLogApi(selectedIds.value);
  if (result.code === 200) {
    ElMessage.success('删除成功');
    if (list.value.length === selectedIds.value.length && currentPage.value > 1) {
      currentPage.value -= 1;
    }
    getList();
  } else {
    ElMessage.error(result.msg || '删除失败');
  }
};

const handleClear = async () => {
  try {
    await ElMessageBox.confirm(
      '清空后所有操作日志都无法恢复，确定继续吗？',
      '清空操作日志',
      { confirmButtonText: '确认清空', cancelButtonText: '取消', type: 'error' }
    );
  } catch {
    return;
  }
  const result = await clearOperLogApi();
  if (result.code === 200) {
    ElMessage.success('已清空操作日志');
    currentPage.value = 1;
    getList();
  } else {
    ElMessage.error(result.msg || '清空失败');
  }
};

onMounted(getList);
</script>

<template>
  <div class="operlog-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span class="card-title">操作日志</span>
          <span class="card-tip">记录新增、修改、删除操作，保留最近 {{ RETENTION_DAYS }} 天</span>
        </div>
      </template>

      <PermissionViewTip :perms="['sys:operlog:delete']" />

      <el-form :model="queryForm" inline class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="操作人">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="用户名 / 昵称 / 操作标题"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="queryForm.moduleName" clearable placeholder="如：友链管理" style="width: 150px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.operType" clearable placeholder="全部" style="width: 110px">
            <el-option
              v-for="item in OPER_LOG_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部" style="width: 100px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button
          v-perm="'sys:operlog:delete'"
          type="danger"
          plain
          icon="Delete"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
        <el-button v-if="isSuperAdmin" type="danger" icon="Delete" @click="handleClear">清空日志</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="list"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="165" />
        <el-table-column label="操作人" width="150">
          <template #default="{ row }">
            <div>{{ row.operNickname || row.operUsername || '—' }}</div>
            <div v-if="row.operNickname && row.operUsername" class="sub-text">{{ row.operUsername }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="模块" width="110" />
        <el-table-column prop="operTitle" label="操作" width="90" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperLogTypeTagType(row.operType)" size="small">
              {{ getOperLogTypeText(row.operType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperLogStatusTagType(row.status)" size="small">
              {{ getOperLogStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90" align="center">
          <template #default="{ row }">{{ row.costTime != null ? `${row.costTime} ms` : '—' }}</template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP" min-width="130" />
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <MyPagination
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @change="getList()"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="日志详情" width="780px" align-center>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作时间">{{ detail.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detail.operNickname || detail.operUsername || '—' }}
          <span v-if="detail.operNickname && detail.operUsername" class="sub-text">
            （{{ detail.operUsername }}）
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.moduleName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ detail.operTitle || '—' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getOperLogTypeText(detail.operType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getOperLogStatusTagType(detail.status)" size="small">
            {{ getOperLogStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod || '—' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ detail.costTime != null ? `${detail.costTime} ms` : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ipAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="环境">
          {{ detail.browser || '—' }} / {{ detail.os || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detail.requestUrl || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-block">
        <div class="detail-label">请求参数</div>
        <pre>{{ prettyJson(detail.operParam) }}</pre>
      </div>
      <div class="detail-block">
        <div class="detail-label">返回结果</div>
        <pre>{{ prettyJson(detail.operResult) }}</pre>
      </div>
      <div v-if="detail.errorMsg" class="detail-block">
        <div class="detail-label error">错误信息</div>
        <pre class="error">{{ detail.errorMsg }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.operlog-page {
  width: 100%;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-tip {
  font-size: 13px;
  color: #909399;
}

.filter-form {
  margin-bottom: 4px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.detail-block {
  margin-top: 16px;
}

.detail-label {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.detail-label.error {
  color: #f56c6c;
}

.detail-block pre {
  max-height: 220px;
  margin: 0;
  padding: 12px 14px;
  overflow: auto;
  border-radius: 6px;
  background: #f7f8fa;
  border: 1px solid #ebeef5;
  font-size: 12px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-block pre.error {
  color: #f56c6c;
}
</style>
