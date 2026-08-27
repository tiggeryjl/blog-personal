// 报表公共工具

// 千分位格式化
export const formatNumber = (value) => {
  if (value === null || value === undefined || value === '') return 0;
  return Number(value).toLocaleString('zh-CN');
};

// "2026-08-25T10:00:00" -> "2026-08-25 10:00"
// 兼容 el-table-column formatter 的 (row, column, cellValue) 调用
export const formatDateTime = (rowOrValue, column, cellValue) => {
  const value = cellValue !== undefined ? cellValue : rowOrValue;
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 16);
};

// 单序列折线图 option
export const buildLineOption = ({ periods, values, name, color = '#409EFF', area = false }) => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [name], top: 0, textStyle: { color: '#606266' } },
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
  series: [
    {
      name,
      type: 'line',
      smooth: true,
      symbolSize: 6,
      itemStyle: { color },
      lineStyle: { width: 2.5 },
      ...(area ? { areaStyle: { opacity: 0.08, color } } : {}),
      data: values,
    },
  ],
});

// 饼图/环形图 option
export const buildPieOption = ({ data, radius = ['38%', '68%'], center = ['50%', '44%'], roseType = false }) => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, textStyle: { color: '#606266' } },
  series: [
    {
      type: 'pie',
      radius,
      center,
      roseType,
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
      data,
    },
  ],
});
