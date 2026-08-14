<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import { getHomeStatisticsApi, getHomeTrendApi, exportHomeTrendApi } from '@/api/home';

// 注册 ECharts 组件
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent]);

// ==================== 统计数据 ====================
const loading = ref(false);
const stats = ref({});

// 统计卡片分组配置：按业务模块归类，key 对应后端返回字段，label 展示名称
const cardGroups = [
  {
    name: '作品管理',
    icon: 'Document',
    color: '#409EFF',
    cards: [
      { key: 'articleTotal', label: '文章总数', icon: 'Document', color: '#409EFF' },
      { key: 'dailyTotal', label: '日常总数', icon: 'Notebook', color: '#67C23A' },
      { key: 'categoryTotal', label: '分类总数', icon: 'Discount', color: '#409EFF' },
      { key: 'tagTotal', label: '标签总数', icon: 'PriceTag', color: '#67C23A' },
      { key: 'viewTotal', label: '总阅读量', icon: 'View', color: '#F56C6C' },
    ],
  },
  {
    name: '友链管理',
    icon: 'Share',
    color: '#7C4DFF',
    cards: [
      { key: 'linkApproved', label: '已通过友链', icon: 'CircleCheck', color: '#67C23A' },
      { key: 'linkPending', label: '待审核友链', icon: 'Clock', color: '#E6A23C' },
      { key: 'linkRejected', label: '已拒绝友链', icon: 'CircleClose', color: '#F56C6C' },
      { key: 'linkTotal', label: '友链总数', icon: 'Share', color: '#909399', subLabel: '包含已拒绝友联' },
    ],
  },
  {
    name: '用户管理',
    icon: 'User',
    color: '#E6A23C',
    cards: [
      { key: 'userTotal', label: '用户总数', icon: 'User', color: '#E6A23C' },
      { key: 'todayNewUser', label: '新增用户数', icon: 'UserFilled', color: '#7C4DFF', subLabel: '今日新增' },
    ],
  },
  {
    name: '评论管理',
    icon: 'ChatLineSquare',
    color: '#F56C6C',
    cards: [
      { key: 'commentTotal', label: '评论总数', icon: 'ChatLineSquare', color: '#F56C6C' },
      { key: 'commentPending', label: '待审核评论', icon: 'ChatDotRound', color: '#E6A23C' },
    ],
  },
];

// 千分位格式化
const formatNumber = (value) => {
  if (value === null || value === undefined) return 0;
  return Number(value).toLocaleString('zh-CN');
};

const statGroups = computed(() =>
  cardGroups.map((group) => ({
    ...group,
    cards: group.cards.map((card) => ({
      ...card,
      value: formatNumber(stats.value[card.key]),
    })),
  }))
);

const getStatistics = async () => {
  loading.value = true;
  try {
    const result = await getHomeStatisticsApi();
    if (result.code == 200) {
      stats.value = result.data || {};
    } else {
      ElMessage.error(result.msg || '统计数据加载失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// ==================== 趋势数据 ====================
const rangeOptions = [
  { key: 'today', label: '今天' },
  { key: 'yesterday', label: '昨天' },
  { key: 'week', label: '近7天' },
  { key: 'month', label: '近30天' },
];
const activeRange = ref('week');
const trendLoading = ref(false);
const trendData = ref([]);

const seriesConfig = [
  { key: 'articleCount', label: '新增文章', color: '#409EFF' },
  { key: 'dailyCount', label: '新增日常', color: '#67C23A' },
  { key: 'userCount', label: '新增用户', color: '#E6A23C' },
  { key: 'commentCount', label: '新增评论', color: '#F56C6C' },
  { key: 'linkCount', label: '新增友链', color: '#7C4DFF' },
];

const getTrend = async () => {
  trendLoading.value = true;
  try {
    const result = await getHomeTrendApi(activeRange.value);
    if (result.code == 200) {
      trendData.value = result.data || [];
    } else {
      ElMessage.error(result.msg || '趋势数据加载失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  } finally {
    trendLoading.value = false;
  }
};

const switchRange = (key) => {
  activeRange.value = key;
  getTrend();
};

// 图表配置
const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    data: seriesConfig.map((item) => item.label),
    top: 0,
    textStyle: { color: '#606266' },
  },
  grid: { left: 45, right: 20, top: 40, bottom: 35 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendData.value.map((item) => item.period),
    axisLabel: {
      color: '#909399',
      formatter: (value) => {
        // "2026-08-09 08:00" -> "08:00"，"2026-08-09" -> "08-09"
        return value.length > 10 ? value.slice(11, 16) : value.slice(5);
      },
    },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    axisLabel: { color: '#909399' },
    splitLine: { lineStyle: { type: 'dashed', color: '#EBEEF5' } },
  },
  series: seriesConfig.map((item) => ({
    name: item.label,
    type: 'line',
    smooth: true,
    symbolSize: 6,
    itemStyle: { color: item.color },
    lineStyle: { width: 2.5 },
    data: trendData.value.map((row) => row[item.key]),
  })),
}));

// 表格合计
const summaryMethod = ({ columns, data }) => {
  const sums = [];
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计';
      return;
    }
    const key = seriesConfig[index - 1]?.key;
    sums[index] = key ? data.reduce((acc, row) => acc + (Number(row[key]) || 0), 0) : '';
  });
  return sums;
};

// 表格空数据
const emptyText = computed(() => (trendLoading.value ? '加载中...' : '暂无数据'));

// ==================== 导出报表 ====================
const activeRangeLabel = computed(
  () => rangeOptions.find((item) => item.key === activeRange.value)?.label || activeRange.value
);

const exportReport = async () => {
  if (!trendData.value.length) {
    ElMessage.warning('暂无趋势数据可导出');
    return;
  }
  try {
    const res = await exportHomeTrendApi(activeRange.value);
    // 后端异常时返回 JSON，而不是 Excel 文件
    if (res.type && res.type.includes('json')) {
      const text = await res.text();
      const data = JSON.parse(text);
      ElMessage.error(data.msg || '导出失败');
      return;
    }
    const now = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const dateStr = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}`;
    const url = URL.createObjectURL(res);
    const link = document.createElement('a');
    link.href = url;
    link.download = `数据趋势报表_${activeRangeLabel.value}_${dateStr}.xlsx`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error('导出失败，请稍后重试');
  }
};

onMounted(() => {
  getStatistics();
  getTrend();
});
</script>

<template>
  <div class="home-container">
    <!-- 头部 -->
    <div class="home-header">
      <div>
        <h1>网站数据概览</h1>
        <p class="home-subtitle">实时掌握博客内容、用户与友链运营情况</p>
      </div>
      <el-button :icon="'Refresh'" @click="getStatistics">刷新数据</el-button>
    </div>

    <!-- 统计卡片分组 -->
    <div v-loading="loading" class="stat-groups">
      <div v-for="group in statGroups" :key="group.name" class="stat-group">
        <div class="group-header">
          <span class="group-icon" :style="{ background: group.color }">
            <el-icon :size="16">
              <component :is="group.icon" />
            </el-icon>
          </span>
          <span class="group-name">{{ group.name }}</span>
        </div>
        <div class="group-cards">
          <div v-for="card in group.cards" :key="card.key" class="stat-card">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="22">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">
                {{ card.label }}
                <el-tag v-if="card.subLabel" size="small" type="warning" class="stat-tag">
                  {{ card.subLabel }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据趋势 -->
    <el-card shadow="never" class="trend-card">
      <div class="trend-header">
        <div class="trend-title">
          <span>数据趋势</span>
          <el-tag size="small" type="info" effect="plain">各时间段新增量</el-tag>
        </div>
        <div class="trend-actions">
          <el-radio-group :model-value="activeRange" size="small" @update:model-value="switchRange">
            <el-radio-button v-for="item in rangeOptions" :key="item.key" :value="item.key">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
          <el-button size="small" type="primary" :icon="'Download'" @click="exportReport"> 导出报表 </el-button>
        </div>
      </div>

      <div v-loading="trendLoading" class="trend-body">
        <v-chart :option="chartOption" autoresize class="trend-chart" />

        <el-table
          :data="trendData"
          border
          stripe
          show-summary
          :summary-method="summaryMethod"
          :empty-text="emptyText"
          class="trend-table"
        >
          <el-table-column label="时间" prop="period" min-width="120" align="center" />
          <el-table-column label="新增文章" prop="articleCount" min-width="90" align="center" />
          <el-table-column label="新增日常" prop="dailyCount" min-width="90" align="center" />
          <el-table-column label="新增用户" prop="userCount" min-width="90" align="center" />
          <el-table-column label="新增评论" prop="commentCount" min-width="90" align="center" />
          <el-table-column label="新增友链" prop="linkCount" min-width="90" align="center" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.home-container {
  padding: 20px;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.home-header h1 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.home-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

/* 统计分组 */
.stat-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: start;
  gap: 16px;
  margin-bottom: 20px;
  min-height: 100px;
}

.stat-group {
  padding: 16px 18px 18px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

.group-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  color: #fff;
  flex-shrink: 0;
}

.group-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.group-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-content: start;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  border-color: var(--el-border-color);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  display: flex;
  align-items: center;
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.stat-tag {
  margin-left: 6px;
}

/* 趋势卡片 */
.trend-card {
  border-radius: 10px;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.trend-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.trend-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trend-body {
  min-height: 300px;
}

.trend-chart {
  height: 340px;
}

.trend-table {
  margin-top: 10px;
}

/* 小屏适配：管理框改为上下排列，卡片减少列数 */
@media (max-width: 1200px) {
  .stat-groups {
    grid-template-columns: 1fr;
  }

  .group-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .group-cards {
    grid-template-columns: 1fr;
  }
}
</style>
