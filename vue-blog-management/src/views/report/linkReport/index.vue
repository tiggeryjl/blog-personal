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

const MODULE = 'link';

const loading = ref(false);
const summary = ref({});
const trendData = ref([]);
const auditDist = ref([]);
const topList = ref([]);
const queryRange = ref({ begin: '', end: '' });

// ==================== KPI 指标卡 ====================
const statItems = computed(() => [
  { label: '友链总数', value: summary.value.linkTotal, icon: 'Share', color: '#7C4DFF' },
  { label: '待审核', value: summary.value.pendingCount, icon: 'Clock', color: '#E6A23C' },
  { label: '已通过', value: summary.value.approvedCount, icon: 'CircleCheck', color: '#67C23A' },
  { label: '已拒绝', value: summary.value.rejectedCount, icon: 'CircleClose', color: '#F56C6C' },
  { label: '本周申请', value: summary.value.newApply, icon: 'Link', color: '#409EFF' },
]);

// ==================== 图表 ====================
const trendOption = computed(() =>
  buildLineOption({
    periods: trendData.value.map((item) => item.period),
    values: trendData.value.map((item) => item.count),
    name: '友链申请',
    color: '#7C4DFF',
    area: true,
  })
);

const auditOption = computed(() => buildPieOption({ data: auditDist.value }));

// ==================== 数据加载 ====================
const loadAll = async () => {
  loading.value = true;
  try {
    const params = { ...queryRange.value };
    const [summaryRes, trendRes, auditRes, topRes] = await Promise.all([
      getReportSummaryApi(MODULE, params),
      getReportTrendApi(MODULE, params),
      getReportDistributionApi(MODULE, params),
      getReportTopApi(MODULE, { limit: 10, ...params }),
    ]);
    if (summaryRes.code == 200) summary.value = summaryRes.data || {};
    if (trendRes.code == 200) trendData.value = trendRes.data || [];
    if (auditRes.code == 200) auditDist.value = auditRes.data || [];
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
      title="友链信息统计"
      subtitle="友链申请、审核状态与最新申请分析"
      @change="handleRangeChange"
      @refresh="loadAll"
    />

    <StatCard :items="statItems" :loading="loading" />

    <div class="chart-row">
      <ChartCard
        title="友链申请趋势"
        class="col-8"
        :loading="loading"
        :empty="!trendData.length"
        :option="trendOption"
      />
      <ChartCard
        title="审核状态分布"
        class="col-4"
        :loading="loading"
        :empty="!auditDist.length"
        :option="auditOption"
      />
    </div>

    <div class="chart-row">
      <TopList
        title="待审核申请 TOP10"
        class="col-12"
        :loading="loading"
        :data="topList"
        :columns="[
          { prop: 'linkName', label: '站点名称', minWidth: 130 },
          { prop: 'linkUrl', label: '网站链接', minWidth: 200 },
          { prop: 'linkEmail', label: '申请邮箱', minWidth: 140 },
          { prop: 'createTime', label: '申请时间', minWidth: 130, align: 'center', tooltip: false, formatter: formatDateTime },
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

.col-12 {
  grid-column: span 12;
}

@media (max-width: 1200px) {
  .col-8,
  .col-4,
  .col-12 {
    grid-column: span 12;
  }
}
</style>
