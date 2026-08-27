<script setup>
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, PieChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components';
import VChart from 'vue-echarts';

// 按需注册 ECharts 组件，避免全局引入
use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent]);

defineProps({
  title: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  empty: { type: Boolean, default: false },
  option: { type: Object, default: () => ({}) },
  height: { type: String, default: '320px' },
});
</script>

<template>
  <el-card shadow="never" class="chart-card">
    <template #header>
      <div class="chart-card-header">
        <span class="chart-card-title">{{ title }}</span>
        <slot name="header" />
      </div>
    </template>
    <div v-loading="loading" class="chart-body" :style="{ height }">
      <v-chart v-if="!empty" :option="option" autoresize class="chart" />
      <el-empty v-else description="暂无数据" :image-size="80" />
    </div>
  </el-card>
</template>

<style scoped>
.chart-card {
  border-radius: 10px;
  height: 100%;
}

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.chart-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-body {
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
