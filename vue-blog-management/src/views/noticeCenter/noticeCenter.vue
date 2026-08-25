<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getNoticeListApi, markReadSingleApi, markReadAllApi, getInitUnreadApi } from '@/api/notice';
import { useNoticeStore } from '@/stores/notice';
import { useRouter } from 'vue-router';

const router = useRouter();
const noticeStore = useNoticeStore();
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const list = ref([]);
const finished = ref(false);

// 通知类型映射
const noticeTypeMap = {
  comment: { text: '评论', type: 'success' },
  like: { text: '点赞', type: 'warning' },
  link: { text: '友链', type: 'warning' },
};

const scrollDisabled = computed(() => loading.value || finished.value);

// 滚动加载：每次请求下一页并追加到列表
const fetchList = async () => {
  if (loading.value || finished.value) return;
  loading.value = true;
  try {
    const res = await getNoticeListApi({ pageNum: page.value, pageSize: pageSize.value });
    if (res.code === 200) {
      list.value.push(...(res.data.rows || []));
      total.value = res.data.total ?? 0;
      finished.value = list.value.length >= total.value;
      page.value += 1;
    }
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  fetchList();
};

const handleReadSingle = async (row) => {
  if (row.isRead) return;
  await markReadSingleApi(row.id);
  row.isRead = 1;
  ElMessage.success('已标记已读');
  const r = await getInitUnreadApi();
  noticeStore.setCount(r.data.unreadTotal);
};

const handleReadAll = async () => {
  await ElMessageBox.confirm('确定全部标记已读？', '提示', { type: 'warning' });
  await markReadAllApi();
  list.value.forEach((item) => (item.isRead = 1));
  ElMessage.success('操作成功');
  noticeStore.clearCount();
};

const goArticle = (row) => {
  if (!row.articleId) return;
  router.push(`/articleDetail?id=${row.articleId}`);
};

onMounted(() => fetchList());
</script>

<template>
  <div class="notice-page">
    <div class="notice-header">
      <h3>消息通知中心</h3>
      <el-button type="primary" @click="handleReadAll">一键标记全部已读</el-button>
    </div>

    <div
      class="notice-scroll"
      v-infinite-scroll="loadMore"
      :infinite-scroll-disabled="scrollDisabled"
      :infinite-scroll-distance="50"
    >
      <el-table :data="list" border v-loading="loading && list.length === 0">
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="noticeTypeMap[scope.row.type]?.type || 'info'">
              {{ noticeTypeMap[scope.row.type]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户" prop="operatorName" width="140" align="center" />
        <el-table-column label="文章标题" prop="articleTitle" width="140" align="center" />
        <el-table-column label="内容" prop="content" min-width="300" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isRead ? 'info' : 'danger'">
              {{ scope.row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="createTime" width="180" align="center" />
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleReadSingle(scope.row)" :disabled="scope.row.isRead"
              >标记已读</el-button
            >
            <el-button size="small" type="primary" @click="goArticle(scope.row)" v-if="scope.row.articleId"
              >查看文章</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div v-if="loading && list.length" class="notice-tip">加载中...</div>
      <div v-else-if="finished && list.length" class="notice-tip">— 没有更多了 —</div>
    </div>
  </div>
</template>

<style scoped>
.notice-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
}
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.notice-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
.notice-tip {
  padding: 14px 0;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
</style>
