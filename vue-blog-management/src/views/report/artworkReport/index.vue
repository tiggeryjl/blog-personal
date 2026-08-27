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
  getWeeklyReportDataApi,
  generateWeeklyReportApi,
} from '@/api/report';
import { formatDateTime, buildPieOption } from '@/utils/report';

const MODULE = 'works';

const loading = ref(false);
const summary = ref({});
const trendData = ref([]);
const categoryDist = ref([]);
const statusDist = ref([]);
const articleTop = ref([]);
const dailyTop = ref([]);
const queryRange = ref({ begin: '', end: '' });

// ==================== KPI 指标卡 ====================
const statItems = computed(() => [
  { label: '文章总数', value: summary.value.articleTotal, icon: 'Document', color: '#409EFF' },
  { label: '日常总数', value: summary.value.dailyTotal, icon: 'Notebook', color: '#67C23A' },
  { label: '总阅读量', value: summary.value.viewTotal, icon: 'View', color: '#F56C6C' },
  { label: '总点赞', value: summary.value.likeTotal, icon: 'Star', color: '#E6A23C' },
  { label: '总评论', value: summary.value.commentTotal, icon: 'ChatLineSquare', color: '#7C4DFF' },
  { label: '分类数', value: summary.value.categoryTotal, icon: 'FolderOpened', color: '#409EFF' },
]);

// ==================== 趋势图（增量/累计） ====================
const trendMode = ref('increment');

const cumsum = (arr) => {
  const result = [];
  let total = 0;
  arr.forEach((value) => {
    total += Number(value) || 0;
    result.push(total);
  });
  return result;
};

const chartOption = computed(() => {
  const periods = trendData.value.map((item) => item.period);
  const isCumulative = trendMode.value === 'cumulative';
  const pick = (key) => trendData.value.map((item) => item[key]);
  const article = isCumulative ? cumsum(pick('articleCount')) : pick('articleCount');
  const daily = isCumulative ? cumsum(pick('dailyCount')) : pick('dailyCount');
  const view = isCumulative ? cumsum(pick('viewCount')) : pick('viewCount');
  const names = isCumulative ? ['累计文章', '累计日常', '累计阅读'] : ['新增文章', '新增日常', '阅读量'];
  const colors = ['#409EFF', '#67C23A', '#F56C6C'];
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: names, top: 0, textStyle: { color: '#606266' } },
    grid: { left: 50, right: 20, top: 40, bottom: 35 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: periods,
      axisLabel: { color: '#909399', formatter: (value) => value.slice(5) },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#909399' },
      splitLine: { lineStyle: { type: 'dashed', color: '#EBEEF5' } },
    },
    series: [article, daily, view].map((values, index) => ({
      name: names[index],
      type: 'line',
      smooth: true,
      symbolSize: 6,
      itemStyle: { color: colors[index] },
      lineStyle: { width: 2.5 },
      data: values,
    })),
  };
});

// ==================== 分布图 ====================
const categoryOption = computed(() => buildPieOption({ data: categoryDist.value, radius: ['0%', '68%'] }));
const statusOption = computed(() => buildPieOption({ data: statusDist.value }));

// ==================== 数据加载 ====================
const loadAll = async () => {
  loading.value = true;
  try {
    const params = { ...queryRange.value };
    const [summaryRes, trendRes, categoryRes, statusRes, articleTopRes, dailyTopRes] = await Promise.all([
      getReportSummaryApi(MODULE, params),
      getReportTrendApi(MODULE, params),
      getReportDistributionApi(MODULE, { dimension: 'category', ...params }),
      getReportDistributionApi(MODULE, { dimension: 'status', ...params }),
      getReportTopApi(MODULE, { metric: 'articleViews', limit: 10, ...params }),
      getReportTopApi(MODULE, { metric: 'dailyLikes', limit: 10, ...params }),
    ]);
    if (summaryRes.code == 200) summary.value = summaryRes.data || {};
    if (trendRes.code == 200) trendData.value = trendRes.data || [];
    if (categoryRes.code == 200) categoryDist.value = categoryRes.data || [];
    if (statusRes.code == 200) statusDist.value = statusRes.data || [];
    if (articleTopRes.code == 200) articleTop.value = articleTopRes.data || [];
    if (dailyTopRes.code == 200) dailyTop.value = dailyTopRes.data || [];
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

// ==================== AI 周报 ====================
const weeklyDialogVisible = ref(false);
const weeklyLoading = ref(false);
const weeklyReportText = ref('');

const generateWeeklyReport = async () => {
  weeklyDialogVisible.value = true;
  weeklyLoading.value = true;
  weeklyReportText.value = '';
  try {
    const dataRes = await getWeeklyReportDataApi();
    if (dataRes.code != 200) {
      ElMessage.error(dataRes.msg || '获取周报数据失败');
      return;
    }
    const aiRes = await generateWeeklyReportApi(dataRes.data);
    if (aiRes.code == 200) {
      weeklyReportText.value = aiRes.data || '';
    } else {
      ElMessage.error(aiRes.msg || 'AI 周报生成失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    weeklyLoading.value = false;
  }
};
</script>

<template>
  <div class="report-page">
    <ReportHeader
      title="作品信息统计"
      subtitle="文章与日常的构成、趋势与榜单分析"
      @change="handleRangeChange"
      @refresh="loadAll"
    >
      <template #extra>
        <el-button type="primary" size="small" :icon="'MagicStick'" @click="generateWeeklyReport">
          生成 AI 周报
        </el-button>
      </template>
    </ReportHeader>

    <StatCard :items="statItems" :loading="loading" />

    <div class="chart-row">
      <ChartCard
        title="作品趋势"
        class="col-8"
        :loading="loading"
        :empty="!trendData.length"
        :option="chartOption"
      >
        <template #header>
          <el-radio-group v-model="trendMode" size="small">
            <el-radio-button value="increment">增量</el-radio-button>
            <el-radio-button value="cumulative">累计</el-radio-button>
          </el-radio-group>
        </template>
      </ChartCard>
      <ChartCard
        title="文章分类分布"
        class="col-4"
        :loading="loading"
        :empty="!categoryDist.length"
        :option="categoryOption"
      />
    </div>

    <div class="chart-row">
      <ChartCard
        title="文章状态分布"
        class="col-4"
        :loading="loading"
        :empty="!statusDist.length"
        :option="statusOption"
      />
      <TopList
        title="阅读量 TOP10 文章"
        class="col-4"
        :loading="loading"
        :data="articleTop"
        :columns="[
          { prop: 'title', label: '标题', minWidth: 130 },
          { prop: 'categoryName', label: '分类', minWidth: 70 },
          { prop: 'viewNum', label: '阅读', minWidth: 60, align: 'center' },
          { prop: 'likeNum', label: '点赞', minWidth: 60, align: 'center' },
          { prop: 'commentNum', label: '评论', minWidth: 60, align: 'center' },
        ]"
      />
      <TopList
        title="点赞 TOP10 日常"
        class="col-4"
        :loading="loading"
        :data="dailyTop"
        :columns="[
          { prop: 'content', label: '内容', minWidth: 130 },
          { prop: 'likeNum', label: '点赞', minWidth: 60, align: 'center' },
          { prop: 'commentNum', label: '评论', minWidth: 60, align: 'center' },
          { prop: 'publishTime', label: '发布时间', minWidth: 95, align: 'center', tooltip: false, formatter: formatDateTime },
        ]"
      />
    </div>

    <!-- AI 周报弹窗 -->
    <el-dialog
      v-model="weeklyDialogVisible"
      title="AI 周报"
      width="640px"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-loading="weeklyLoading" class="weekly-report-body">
        <template v-if="weeklyReportText">
          <div class="weekly-report-text">{{ weeklyReportText }}</div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="数据口径：近7天阅读量（阅读记录表）、新增评论/文章/用户，热门文章按本周阅读量排序"
            class="weekly-report-tip"
          />
        </template>
        <el-empty v-else-if="!weeklyLoading" description="点击「生成 AI 周报」后此处展示结果" :image-size="80" />
      </div>
    </el-dialog>
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

.weekly-report-body {
  min-height: 200px;
}

.weekly-report-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
  max-height: 420px;
  overflow-y: auto;
}

.weekly-report-tip {
  margin-top: 14px;
}

@media (max-width: 1200px) {
  .col-8,
  .col-4 {
    grid-column: span 12;
  }
}
</style>
