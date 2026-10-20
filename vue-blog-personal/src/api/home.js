import request from '@/utils/request';

// 获取首页热门文章，后端固定最多返回20条
export const getPopularArticleListApi = () => request.get('/user/home/popular');

// 获取首页公告，没有启用或无内容时后端返回 null
export const getHomeNoticeApi = () => request.get('/user/home/notice');
