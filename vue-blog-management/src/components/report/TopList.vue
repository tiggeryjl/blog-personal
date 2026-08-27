<script setup>
defineProps({
  title: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  columns: { type: Array, default: () => [] },
  data: { type: Array, default: () => [] },
  emptyText: { type: String, default: '暂无数据' },
});
</script>

<template>
  <el-card shadow="never" class="top-list-card">
    <template #header>
      <span class="top-list-title">{{ title }}</span>
    </template>
    <el-table v-loading="loading" :data="data" :empty-text="emptyText" class="top-list-table">
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :min-width="col.minWidth || 120"
        :align="col.align || 'left'"
        :show-overflow-tooltip="col.tooltip !== false"
        :formatter="col.formatter"
      />
    </el-table>
  </el-card>
</template>

<style scoped>
.top-list-card {
  border-radius: 10px;
  height: 100%;
}

.top-list-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.top-list-table {
  width: 100%;
}
</style>
