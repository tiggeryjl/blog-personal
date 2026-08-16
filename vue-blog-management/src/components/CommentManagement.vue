<script setup>
import { ref, reactive, watch, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import MyPagination from '@/components/MyPagination.vue';
import {
  Search,
  Refresh,
  Delete,
  Top,
  Hide,
  Check,
  ChatLineRound,
  CaretRight,
  Expand,
  Fold,
} from '@element-plus/icons-vue';
import {
  getArticleCommentListApi,
  getDailyCommentListApi,
  getMessageCommentListApi,
  addCommentReplyApi,
  updateCommentStatusApi,
  updateCommentTopApi,
  logicDeleteCommentApi,
} from '@/api/comment.js';
import {
  COMMENT_STATUS,
  COMMENT_STATUS_TEXT,
  getCommentStatusText,
  getCommentStatusType,
  getMessageTypeText,
  MESSAGE_TYPE_TEXT,
} from '@/constants/commentConstants';

/**
 * 评论管理共用组件
 * commentType: 0文章评论 1日常评论 2留言评论
 */
const props = defineProps({
  commentType: { type: Number, required: true },
  pageTitle: { type: String, required: true },
  showMsgType: { type: Boolean, default: false },
  showSourceId: { type: Boolean, default: true },
});

// 按评论类型选择列表接口
const listApiMap = {
  0: getArticleCommentListApi,
  1: getDailyCommentListApi,
  2: getMessageCommentListApi,
};

// 查询条件
const queryForm = reactive({
  keyword: '',
  sourceId: '',
  msgType: '',
  status: '',
  createTime: [],
  begin: '',
  end: '',
});

watch(
  () => queryForm.createTime,
  (newVal) => {
    if (newVal && newVal.length === 2) {
      queryForm.begin = newVal[0];
      queryForm.end = newVal[1];
    } else {
      queryForm.begin = '';
      queryForm.end = '';
    }
  }
);

// 评论列表
const commentList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const tableRef = ref(null);

/**
 * 将平铺评论列表组装成树形结构：
 * 主楼评论作为一级节点，回复挂到对应父评论下，多级回复逐层嵌套；
 * 父评论不在当前页时，保留为一级节点并标记 isOrphan。
 */
const buildCommentTree = (list) => {
  const map = new Map();
  list.forEach((item) => {
    map.set(item.id, { ...item, children: [] });
  });

  const roots = [];
  list.forEach((item) => {
    const node = map.get(item.id);
    const parentId = item.parentId;
    if (parentId && parentId !== 0 && map.has(parentId)) {
      node.depth = map.get(parentId).depth + 1;
      map.get(parentId).children.push(node);
    } else {
      if (parentId && parentId !== 0) {
        node.isOrphan = true;
      }
      node.depth = 0;
      roots.push(node);
    }
  });

  // 回复按时间正序排列（主楼下的楼层从早到晚），便于阅读
  const sortByTime = (a, b) => {
    const timeA = a.createTime || '';
    const timeB = b.createTime || '';
    return timeA.localeCompare(timeB);
  };

  const normalize = (node) => {
    if (!node.children.length) {
      node.children = undefined;
    } else {
      node.children.sort(sortByTime);
      node.children.forEach(normalize);
    }
  };

  roots.forEach(normalize);
  return roots;
};

const treeList = computed(() => buildCommentTree(commentList.value));

// 嵌套回复行按层级使用递进加深的底色，增强主楼/回复的层次感
const getRowClassName = ({ row }) => {
  if (!row.depth || row.depth === 0) return '';
  return row.depth >= 3 ? 'reply-depth-3' : `reply-depth-${row.depth}`;
};

// ==================== 展开/折叠控制 ====================
// 记录各行展开状态(默认全部展开)，与 el-table 内部树形展开状态保持一致
const expandedMap = ref({});

const hasChildren = (row) => !!(row.children && row.children.length > 0);

const isExpanded = (row) => !!expandedMap.value[row.id];

const toggleRow = (row) => {
  const next = !isExpanded(row);
  expandedMap.value[row.id] = next;
  tableRef.value?.toggleRowExpansion(row, next);
};

// 是否所有主楼都已展开
const allExpanded = computed(() => {
  const roots = treeList.value;
  if (!roots.length) return false;
  return roots.every((root) => !hasChildren(root) || isExpanded(root));
});

// 全部展开/折叠主楼
const toggleAllRows = () => {
  const target = !allExpanded.value;
  treeList.value.forEach((root) => {
    if (hasChildren(root)) {
      expandedMap.value[root.id] = target;
      tableRef.value?.toggleRowExpansion(root, target);
    }
  });
};

const resetExpandedMap = () => {
  expandedMap.value = {};
  treeList.value.forEach((root) => {
    if (hasChildren(root)) {
      expandedMap.value[root.id] = true;
    }
  });
};

const getCommentList = async () => {
  loading.value = true;
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value,
  };
  delete params.createTime;
  try {
    const result = await listApiMap[props.commentType](params);
    if (result.code === 200) {
      commentList.value = result.data.rows || [];
      total.value = result.data.total || 0;
      resetExpandedMap();
    } else {
      ElMessage.error(result.msg || '获取评论列表失败');
      commentList.value = [];
      total.value = 0;
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
    commentList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// ==================== 后台回复评论 ====================
const replyDialogVisible = ref(false);
const replyRow = ref(null);
const replyContent = ref('');

const openReply = (row) => {
  replyRow.value = row;
  replyContent.value = '';
  replyDialogVisible.value = true;
};

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  try {
    const result = await addCommentReplyApi({
      parentId: replyRow.value.id,
      content: replyContent.value.trim(),
    });
    if (result.code === 200) {
      ElMessage.success('回复成功');
      replyDialogVisible.value = false;
      getCommentList();
    } else {
      ElMessage.error(result.msg || '回复失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
};

// 重置查询
const resetQuery = () => {
  queryForm.keyword = '';
  queryForm.sourceId = '';
  queryForm.msgType = '';
  queryForm.status = '';
  queryForm.createTime = [];
  queryForm.begin = '';
  queryForm.end = '';
  currentPage.value = 1;
  getCommentList();
};

// 审核：显示/隐藏
const handleAudit = async (row, status) => {
  const actionText = status === COMMENT_STATUS.NORMAL ? '显示' : '隐藏';
  try {
    const result = await updateCommentStatusApi({ id: row.id, status });
    if (result.code === 200) {
      ElMessage.success(`${actionText}成功`);
      getCommentList();
    } else {
      ElMessage.error(result.msg || `${actionText}失败`);
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
};

// 置顶/取消置顶
const handleTop = async (row) => {
  try {
    const result = await updateCommentTopApi(row.id);
    if (result.code === 200) {
      ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已置顶');
      getCommentList();
    } else {
      ElMessage.error(result.msg || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
};

// 删除单条
const handleDelete = (row) => {
  ElMessageBox.confirm('您确认要删除该评论吗？其下的回复将一并删除。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        const result = await logicDeleteCommentApi(row.id);
        if (result.code === 200) {
          ElMessage.success('删除成功');
          getCommentList();
        } else {
          ElMessage.error(result.msg || '删除失败');
        }
      } catch (error) {
        ElMessage.error('网络请求失败，请稍后重试');
      }
    })
    .catch(() => {});
};

// 批量删除
const selectedIds = ref([]);
const handleSelectionChange = (val) => {
  selectedIds.value = val.map((item) => item.id);
};

const handleBatchDelete = () => {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选要删除的评论');
    return;
  }
  ElMessageBox.confirm(`您确认要删除选中的 ${selectedIds.value.length} 条评论吗？其下的回复将一并删除。`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        const result = await logicDeleteCommentApi(selectedIds.value);
        if (result.code === 200) {
          ElMessage.success('批量删除成功');
          getCommentList();
        } else {
          ElMessage.error(result.msg || '批量删除失败');
        }
      } catch (error) {
        ElMessage.error('网络请求失败，请稍后重试');
      }
    })
    .catch(() => {});
};

onMounted(() => {
  getCommentList();
});
</script>

<template>
  <div class="comment-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>{{ pageTitle }}</h1>
      <div class="header-actions">
        <el-button type="primary" :icon="allExpanded ? Fold : Expand" @click="toggleAllRows">
          {{ allExpanded ? '全部折叠' : '全部展开' }}
        </el-button>
        <el-button v-perm="'sys:comment:delete'" type="danger" :icon="Delete" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 查询条件区域 -->
    <el-card class="query-card" shadow="hover">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getCommentList">
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.keyword"
            placeholder="评论内容/评论人昵称"
            clearable
            style="width: 220px"
            @keyup.enter="getCommentList"
          />
        </el-form-item>

        <el-form-item v-if="showSourceId" label="来源ID">
          <el-input
            v-model="queryForm.sourceId"
            placeholder="文章/日常ID"
            clearable
            style="width: 130px"
            @keyup.enter="getCommentList"
          />
        </el-form-item>

        <el-form-item v-if="showMsgType" label="留言类型">
          <el-select v-model="queryForm.msgType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in MESSAGE_TYPE_TEXT" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(text, value) in COMMENT_STATUS_TEXT" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>

        <el-form-item label="评论时间">
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
          <el-button type="primary" :icon="Search" @click="getCommentList">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="list-card" shadow="hover">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="treeList"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :row-class-name="getRowClassName"
        :indent="28"
        default-expand-all
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />

        <el-table-column label="评论人" width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <button
                class="tree-toggle-btn"
                :class="{ 'is-placeholder': !hasChildren(row) }"
                @click.stop="hasChildren(row) && toggleRow(row)"
              >
                <el-icon class="tree-arrow" :class="{ 'is-expanded': isExpanded(row) }">
                  <CaretRight />
                </el-icon>
              </button>
              <el-avatar :size="34" :src="row.userAvatar || ''">
                {{ row.userNickname ? row.userNickname.charAt(0) : '' }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ row.userNickname || '—' }}</div>
                <div class="user-id">ID: {{ row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="240">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-text">{{ row.content }}</div>
              <el-tag v-if="row.parentId && row.parentId !== 0" size="small" type="info" class="reply-tag">
                回复 @{{ row.replyUserNickname || row.parentNickname || '—' }}
              </el-tag>
              <el-tag v-if="row.isOrphan" size="small" type="warning" class="reply-tag"> 原主楼不在本页 </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sourceTitle" label="来源" min-width="160" align="center">
          <template #default="{ row }">
            <div class="source-cell">
              <div class="source-title">{{ row.sourceTitle || '—' }}</div>
              <div class="source-id">来源ID: {{ row.sourceId }}</div>
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

        <el-table-column label="置顶" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isTop === 1 ? 'warning' : 'info'">
              {{ row.isTop === 1 ? '已置顶' : '未置顶' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="点赞/回复" width="100" align="center">
          <template #default="{ row }">
            <div class="num-cell">
              <div>点赞 {{ row.likeNum ?? 0 }}</div>
              <div>回复 {{ row.replyCount ?? 0 }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="设备信息" width="150" align="center">
          <template #default="{ row }">
            <div class="device-cell">
              <div>{{ row.location || '—' }}</div>
              <div class="device-sub">{{ row.browser || '' }} {{ row.os || '' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评论时间" width="150" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <div>{{ row.createTime || '—' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button v-perm="'sys:comment:audit'" type="success" link :icon="ChatLineRound" @click="openReply(row)">
                回复
              </el-button>
              <el-button
                v-perm="'sys:comment:audit'"
                v-if="row.status === COMMENT_STATUS.HIDDEN"
                type="success"
                link
                :icon="Check"
                @click="handleAudit(row, COMMENT_STATUS.NORMAL)"
              >
                显示
              </el-button>
              <el-button
                v-perm="'sys:comment:audit'"
                v-else
                type="warning"
                link
                :icon="Hide"
                @click="handleAudit(row, COMMENT_STATUS.HIDDEN)"
              >
                隐藏
              </el-button>
              <el-button v-perm="'sys:comment:audit'" type="primary" link :icon="Top" @click="handleTop(row)">
                {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button v-perm="'sys:comment:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-box">
        <MyPagination
          :total="total"
          :current-page="currentPage"
          :page-size="pageSize"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
          @change="getCommentList"
        />
      </div>
    </el-card>

    <!-- 回复评论弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="520px">
      <div v-if="replyRow" class="reply-target">
        <span class="reply-target-label">回复 @{{ replyRow.userNickname || '—' }}</span>
        <div class="reply-target-content">{{ replyRow.content }}</div>
      </div>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="请输入回复内容..."
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确认回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.comment-management-container {
  padding: 0px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-card,
.list-card {
  margin-bottom: 20px;
}

.pagination-box {
  margin-top: 20px;
  text-align: right;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;
}

.tree-toggle-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  background: transparent;
  color: #909399;
  cursor: pointer;
  flex-shrink: 0;
}

.tree-toggle-btn:hover {
  color: #409eff;
}

/* 无子回复的行也预留三角位置，保证同层头像对齐 */
.tree-toggle-btn.is-placeholder {
  visibility: hidden;
}

.tree-arrow {
  font-size: 14px;
  transition: transform 0.2s ease;
}

.tree-arrow.is-expanded {
  transform: rotate(90deg);
}

.user-info {
  text-align: left;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.content-cell {
  text-align: left;
}

.content-text {
  word-break: break-all;
  line-height: 1.5;
  white-space: pre-wrap;
}

.reply-tag {
  margin-top: 6px;
}

.source-cell {
  text-align: left;
}

.source-title {
  word-break: break-all;
  line-height: 1.4;
}

.source-id {
  font-size: 12px;
  color: #999;
}

.num-cell {
  line-height: 1.6;
  font-size: 13px;
}

.device-cell {
  line-height: 1.5;
  font-size: 13px;
}

.device-sub {
  font-size: 12px;
  color: #999;
}

.time-cell {
  font-size: 13px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 8px;
  justify-items: center;
}

.action-buttons .el-button {
  margin: 0;
}

/* 隐藏 el-table 树形模式自带的展开箭头，改用自定义三角 */
:deep(.el-table__expand-icon) {
  display: none;
}

/* 嵌套回复行底色：层级越深颜色越深，突出主楼下的回复楼层 */
:deep(.reply-depth-1 td.el-table__cell) {
  background-color: #daf8ff !important;
}

:deep(.reply-depth-2 td.el-table__cell) {
  background-color: #c0dde4 !important;
}

:deep(.reply-depth-3 td.el-table__cell) {
  background-color: #abccd4 !important;
}

/* 回复行内容左侧加一条楼层线，强化"挂在主楼下"的视觉引导 */
:deep(.reply-depth-1 .content-cell),
:deep(.reply-depth-2 .content-cell),
:deep(.reply-depth-3 .content-cell) {
  border-left: 3px solid #008cff;
  padding-left: 10px;
}

.reply-target {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 14px;
}

.reply-target-label {
  color: #409eff;
  font-weight: 500;
  margin-bottom: 6px;
  display: inline-block;
}

.reply-target-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-all;
  max-height: 80px;
  overflow-y: auto;
}
</style>

<!-- 兜底：确保 el-table 树形模式自带的展开箭头不显示（scoped :deep 可能因组件根节点
     未继承作用域属性而失效，这里用容器类限定范围，只影响评论管理表格） -->
<style>
.comment-management-container .el-table__expand-icon {
  display: none !important;
}
</style>
