// 小于1万显示千分位（1,234），大于等于1万显示 x.x万
export const formatCount = (value) => {
  const num = Number(value) || 0;
  if (num >= 10000) {
    const wan = num / 10000;
    const text = wan >= 100 ? String(Math.round(wan)) : wan.toFixed(1).replace(/\.0$/, '');
    return `${text}万`;
  }
  return num.toLocaleString('zh-CN');
};
