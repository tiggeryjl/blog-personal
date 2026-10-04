import request from '@/utils/request';

//获取文章列表
export const getArticleListApi = (params) => request.get(`/user/article/getArticleList`, { params });

//获取日历上每天的文章数量（用于给有文章的日期加标识）
export const getArticleCalendarApi = (params) => request.get(`/user/article/getCalendarCount`, { params });

//根据文章id获取文章详情
export const getArticleDetailApi = (id) => request.get(`/user/article/getArticleDetail/${id}`);
