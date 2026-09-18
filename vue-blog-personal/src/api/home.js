import request from '@/utils/request';

// 获取首页热门文章，后端固定最多返回20条
export const getPopularArticleListApi = () => request.get('/user/home/popular');
