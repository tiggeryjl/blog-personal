<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
});

const emit = defineEmits(['change', 'refresh']);

const rangeOptions = [
  { key: 'week', label: '近7天' },
  { key: 'month', label: '近30天' },
  { key: 'custom', label: '自定义' },
];

const activeRange = ref('week');
// 已确认的自定义日期（用于查询与头部展示）
const customBegin = ref('');
const customEnd = ref('');
// 下拉面板中的临时草稿，取消时不生效
const draftBegin = ref('');
const draftEnd = ref('');
const popoverVisible = ref(false);

const rangeLabel = computed(() => {
  if (activeRange.value === 'custom') {
    return customBegin.value && customEnd.value ? `${customBegin.value} ~ ${customEnd.value}` : '';
  }
  return rangeOptions.find((item) => item.key === activeRange.value)?.label || '';
});

const formatDate = (date) => {
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const emitChange = () => {
  const today = new Date();
  let begin;
  let end;
  if (activeRange.value === 'custom') {
    if (!customBegin.value || !customEnd.value) return;
    if (customBegin.value > customEnd.value) {
      ElMessage.warning('开始日期不能晚于结束日期');
      return;
    }
    begin = customBegin.value;
    end = customEnd.value;
  } else {
    const days = activeRange.value === 'week' ? 6 : 29;
    begin = formatDate(new Date(today.getFullYear(), today.getMonth(), today.getDate() - days));
    end = formatDate(today);
  }
  emit('change', { begin, end, label: rangeLabel.value });
};

const switchRange = (key) => {
  activeRange.value = key;
  if (key === 'custom') {
    // 打开面板时用已确认值初始化草稿，未选过则默认近7天
    const today = new Date();
    draftBegin.value =
      customBegin.value || formatDate(new Date(today.getFullYear(), today.getMonth(), today.getDate() - 6));
    draftEnd.value = customEnd.value || formatDate(today);
    return;
  }
  emitChange();
};

// 待选日期快捷项
const presets = [
  { key: 'today', label: '今天' },
  { key: 'week', label: '近7天' },
  { key: 'month', label: '近30天' },
  { key: 'thisMonth', label: '本月' },
  { key: 'lastMonth', label: '上月' },
];

const applyPreset = (preset) => {
  const today = new Date();
  let begin = today;
  let end = today;
  switch (preset.key) {
    case 'today':
      break;
    case 'week':
      begin = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 6);
      break;
    case 'month':
      begin = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 29);
      break;
    case 'thisMonth':
      begin = new Date(today.getFullYear(), today.getMonth(), 1);
      end = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      break;
    case 'lastMonth':
      begin = new Date(today.getFullYear(), today.getMonth() - 1, 1);
      end = new Date(today.getFullYear(), today.getMonth(), 0);
      break;
  }
  draftBegin.value = formatDate(begin);
  draftEnd.value = formatDate(end);
};

const applyCustom = () => {
  if (!draftBegin.value || !draftEnd.value) {
    ElMessage.warning('请选择开始和结束日期');
    return;
  }
  if (draftBegin.value > draftEnd.value) {
    ElMessage.warning('开始日期不能晚于结束日期');
    return;
  }
  customBegin.value = draftBegin.value;
  customEnd.value = draftEnd.value;
  emitChange();
  popoverVisible.value = false;
};

const cancelCustom = () => {
  popoverVisible.value = false;
};

onMounted(() => {
  emitChange();
});
</script>

<template>
  <div class="report-header">
    <div class="report-header-left">
      <h2>{{ title }}</h2>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <div class="report-header-right">
      <el-radio-group :model-value="activeRange" size="small" @update:model-value="switchRange">
        <el-radio-button value="week">近7天</el-radio-button>
        <el-radio-button value="month">近30天</el-radio-button>
        <el-popover v-model:visible="popoverVisible" placement="bottom-start" :width="470" trigger="click">
          <template #reference>
            <el-radio-button value="custom">自定义</el-radio-button>
          </template>
          <div class="custom-range-panel">
            <!-- 第一行：日期选择器 -->
            <div class="custom-range-row">
              <el-date-picker
                v-model="draftBegin"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                :clearable="false"
                :teleported="false"
                size="small"
                class="custom-date"
              />
              <span class="custom-range-sep">至</span>
              <el-date-picker
                v-model="draftEnd"
                type="date"
                placeholder="结束日期"
                value-format="YYYY-MM-DD"
                :clearable="false"
                :teleported="false"
                size="small"
                class="custom-date"
              />
            </div>
            <!-- 第二行：待选日期 -->
            <div class="custom-preset-row">
              <span class="preset-label">待选日期</span>
              <el-tag
                v-for="preset in presets"
                :key="preset.key"
                effect="plain"
                class="preset-tag"
                @click="applyPreset(preset)"
              >
                {{ preset.label }}
              </el-tag>
            </div>
            <!-- 第三行：确认/取消 -->
            <div class="custom-range-footer">
              <el-button size="small" @click="cancelCustom">取消</el-button>
              <el-button size="small" type="primary" @click="applyCustom">确认</el-button>
            </div>
          </div>
        </el-popover>
      </el-radio-group>
      <slot name="extra" />
      <el-button size="small" :icon="'Refresh'" @click="emit('refresh')">刷新</el-button>
    </div>
  </div>
</template>

<style scoped>
.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.report-header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.report-header-left p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

.report-header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

<!-- 自定义日期面板渲染在 body 下，需要非 scoped 样式 -->
<style>
.custom-range-panel {
  padding: 12px 14px;
}

.custom-range-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom-date {
  width: 150px;
}

.custom-range-sep {
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
}

.custom-range-footer {
  margin-top: 14px;
  text-align: right;
}

.custom-preset-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
  padding: 12px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.preset-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

.preset-tag {
  cursor: pointer;
}

.preset-tag:hover {
  color: #409eff;
  border-color: #409eff;
}
</style>
