import request from '@/utils/request';

//获取文章列表
export const getArticleListApi = (params) => request.get(`/user/article/getArticleList`, { params });

//根据文章id获取文章详情
export const getArticleDetailApi = (id) => request.get(`/user/article/getArticleDetail/${id}`);
