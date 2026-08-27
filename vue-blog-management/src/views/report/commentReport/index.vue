<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import ReportHeader from '@/components/report/ReportHeader.vue';
import StatCard from '@/components/report/StatCard.vue';
import ChartCard from '@/components/report/ChartCard.vue';
import TopList from '@/components/report/TopList.vue';
import {
  getReportSummaryApi,
  getReportTrendApi,
  getReportDistributionApi,
  getReportTopApi,
} from '@/api/report';
import { formatDateTime, buildLineOption, buildPieOption } from '@/utils/report';

const MODULE = 'comment';

const loading = ref(false);
const summary = ref({});
const trendData = ref([]);
const typeDist = ref([]);
const statusDist = ref([]);
const topList = ref([]);
const queryRange = ref({ begin: '', end: '' });

// ==================== KPI 指标卡 ====================
const statItems = computed(() => [
  { label: '评论总数', value: summary.value.commentTotal, icon: 'ChatLineSquare', color: '#F56C6C' },
  { label: '本周新增', value: summary.value.newComment, icon: 'ChatDotRound', color: '#409EFF' },
  { label: '正常评论', value: summary.value.normalCount, icon: 'CircleCheck', color: '#67C23A' },
  { label: '隐藏评论', value: summary.value.hiddenCount, icon: 'Hide', color: '#E6A23C' },
]);

// ==================== 图表 ====================
const trendOption = computed(() =>
  buildLineOption({
    periods: trendData.value.map((item) => item.period),
    values: trendData.value.map((item) => item.count),
    name: '新增评论',
    color: '#F56C6C',
    area: true,
  })
);

const typeOption = computed(() => buildPieOption({ data: typeDist.value, radius: ['0%', '68%'] }));
const statusOption = computed(() => buildPieOption({ data: statusDist.value }));

// ==================== 数据加载 ====================
const loadAll = async () => {
  loading.value = true;
  try {
    const params = { ...queryRange.value };
    const [summaryRes, trendRes, typeRes, statusRes, topRes] = await Promise.all([
      getReportSummaryApi(MODULE, params),
      getReportTrendApi(MODULE, params),
      getReportDistributionApi(MODULE, { dimension: 'type', ...params }),
      getReportDistributionApi(MODULE, { dimension: 'status', ...params }),
      getReportTopApi(MODULE, { limit: 10, ...params }),
    ]);
    if (summaryRes.code == 200) summary.value = summaryRes.data || {};
    if (trendRes.code == 200) trendData.value = trendRes.data || [];
    if (typeRes.code == 200) typeDist.value = typeRes.data || [];
    if (statusRes.code == 200) statusDist.value = statusRes.data || [];
    if (topRes.code == 200) topList.value = topRes.data || [];
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleRangeChange = (range) => {
  queryRange.value = { begin: range.begin, end: range.end };
  loadAll();
};
</script>

<template>
  <div class="report-page">
    <ReportHeader
      title="评论信息统计"
      subtitle="评论的构成、趋势与热门内容分析"
      @change="handleRangeChange"
      @refresh="loadAll"
    />

    <StatCard :items="statItems" :loading="loading" />

    <div class="chart-row">
      <ChartCard
        title="评论新增趋势"
        class="col-8"
        :loading="loading"
        :empty="!trendData.length"
        :option="trendOption"
      />
      <ChartCard
        title="评论类型分布"
        class="col-4"
        :loading="loading"
        :empty="!typeDist.length"
        :option="typeOption"
      />
    </div>

    <div class="chart-row">
      <ChartCard
        title="评论状态分布"
        class="col-4"
        :loading="loading"
        :empty="!statusDist.length"
        :option="statusOption"
      />
      <TopList
        title="评论最多文章 TOP10"
        class="col-8"
        :loading="loading"
        :data="topList"
        :columns="[
          { prop: 'title', label: '文章标题', minWidth: 200 },
          { prop: 'commentCount', label: '评论数', minWidth: 90, align: 'center' },
          { prop: 'lastTime', label: '最近评论时间', minWidth: 140, align: 'center', tooltip: false, formatter: formatDateTime },
        ]"
      />
    </div>
  </div>
</template>

<style scoped>
.report-page {
  padding: 20px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
  align-items: stretch;
}

.col-8 {
  grid-column: span 8;
}

.col-4 {
  grid-column: span 4;
}

@media (max-width: 1200px) {
  .col-8,
  .col-4 {
    grid-column: span 12;
  }
}
</style>
