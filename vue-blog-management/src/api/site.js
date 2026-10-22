import request from '@/utils/request';

// 获取站点信息（友链页展示）
export const getSiteProfileApi = () => request.get('/admin/site/profile');

// 保存站点信息
export const saveSiteProfileApi = (data) => request.put('/admin/site/profile', data);
