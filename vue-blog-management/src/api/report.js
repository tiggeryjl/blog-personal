import request from '@/utils/request';

// 报表汇总指标 module: works/comment/user/link
export const getReportSummaryApi = (module, params) =>
  request.get(`/admin/report/${module}/summary`, { params });

// 按天趋势
export const getReportTrendApi = (module, params) =>
  request.get(`/admin/report/${module}/trend`, { params });

// 分布数据 dimension: works=category/status，comment=type/status
export const getReportDistributionApi = (module, params) =>
  request.get(`/admin/report/${module}/distribution`, { params });

// 榜单数据 metric: works=articleViews/dailyLikes
export const getReportTopApi = (module, params) =>
  request.get(`/admin/report/${module}/top`, { params });

// AI 周报基础数据（近7天）
export const getWeeklyReportDataApi = () => request.get('/admin/report/weeklyData');

// 生成 AI 周报
export const generateWeeklyReportApi = (data) => request.post('/AiChat/ai/weekly-report', data);
