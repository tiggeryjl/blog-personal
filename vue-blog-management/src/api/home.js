import request from '@/utils/request';

// 获取首页网站统计数据
export const getHomeStatisticsApi = () => request.get('/admin/home/getStatistics');

// 获取首页趋势数据 rangeType: today=今天 yesterday=昨天 week=近7天 month=近30天
export const getHomeTrendApi = (rangeType) => request.get('/admin/home/getTrend', { params: { rangeType } });

// 导出首页数据趋势报表 rangeType: today=今天 yesterday=昨天 week=近7天 month=近30天
export const exportHomeTrendApi = (rangeType) =>
  request.get('/admin/home/exportTrend', { params: { rangeType }, responseType: 'blob' });

// 获取首页公告配置（未启用时也返回，供编辑）
export const getHomeNoticeConfigApi = () => request.get('/admin/home/notice');

// 保存首页公告
export const saveHomeNoticeApi = (data) => request.put('/admin/home/notice', data);
