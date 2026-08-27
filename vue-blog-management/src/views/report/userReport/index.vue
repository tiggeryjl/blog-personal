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
import { buildLineOption, buildPieOption } from '@/utils/report';

const MODULE = 'user';

const loading = ref(false);
const summary = ref({});
const trendData = ref([]);
const activityDist = ref([]);
const topList = ref([]);
const queryRange = ref({ begin: '', end: '' });

// ==================== KPI 指标卡 ====================
const statItems = computed(() => [
  { label: '用户总数', value: summary.value.userTotal, icon: 'User', color: '#E6A23C' },
  { label: '本周新增', value: summary.value.newUser, icon: 'UserFilled', color: '#409EFF' },
  { label: '今日新增', value: summary.value.todayNewUser, icon: 'AlarmClock', color: '#67C23A' },
  { label: '活跃用户', value: summary.value.activeUserCount, icon: 'Lightning', color: '#F56C6C' },
  { label: '禁用账号', value: summary.value.disabledCount, icon: 'CircleClose', color: '#909399' },
]);

// ==================== 图表 ====================
const trendOption = computed(() =>
  buildLineOption({
    periods: trendData.value.map((item) => item.period),
    values: trendData.value.map((item) => item.count),
    name: '新增用户',
    color: '#E6A23C',
    area: true,
  })
);

const activityOption = computed(() => buildPieOption({ data: activityDist.value, radius: ['0%', '68%'] }));

// ==================== 数据加载 ====================
const loadAll = async () => {
  loading.value = true;
  try {
    const params = { ...queryRange.value };
    const [summaryRes, trendRes, activityRes, topRes] = await Promise.all([
      getReportSummaryApi(MODULE, params),
      getReportTrendApi(MODULE, params),
      getReportDistributionApi(MODULE, params),
      getReportTopApi(MODULE, { limit: 10, ...params }),
    ]);
    if (summaryRes.code == 200) summary.value = summaryRes.data || {};
    if (trendRes.code == 200) trendData.value = trendRes.data || [];
    if (activityRes.code == 200) activityDist.value = activityRes.data || [];
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
      title="用户信息统计"
      subtitle="用户注册、活跃度与行为分析"
      @change="handleRangeChange"
      @refresh="loadAll"
    />

    <StatCard :items="statItems" :loading="loading" />

    <div class="chart-row">
      <ChartCard
        title="用户注册趋势"
        class="col-8"
        :loading="loading"
        :empty="!trendData.length"
        :option="trendOption"
      />
      <ChartCard
        title="用户活跃度分布"
        class="col-4"
        :loading="loading"
        :empty="!activityDist.length"
        :option="activityOption"
      />
    </div>

    <div class="chart-row">
      <TopList
        title="活跃用户 TOP10"
        class="col-12"
        :loading="loading"
        :data="topList"
        :columns="[
          { prop: 'nickname', label: '昵称', minWidth: 150 },
          { prop: 'articleCount', label: '发文数', minWidth: 90, align: 'center' },
          { prop: 'commentCount', label: '评论数', minWidth: 90, align: 'center' },
          { prop: 'activityCount', label: '活跃度', minWidth: 90, align: 'center' },
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
