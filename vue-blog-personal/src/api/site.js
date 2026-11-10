import request from '@/utils/request';

// 获取站点信息（友链页展示），未配置时后端返回空配置
export const getSiteProfileApi = () => request.get('/user/site/profile');

// 获取网站备案信息，未配置时返回空值
export const getSiteFilingApi = () => request.get('/user/site/filing');
