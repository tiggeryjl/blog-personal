<script setup>
import { formatNumber } from '@/utils/report';

defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
});
</script>

<template>
  <div v-loading="loading" class="stat-card-row">
    <div v-for="item in items" :key="item.label" class="stat-card">
      <div class="stat-icon" :style="{ background: item.color || '#409EFF' }">
        <el-icon :size="20">
          <component :is="item.icon || 'DataLine'" />
        </el-icon>
      </div>
      <div class="stat-info">
        <div class="stat-value">{{ formatNumber(item.value) }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
  min-height: 82px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 21px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 1400px) {
  .stat-card-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .stat-card-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
