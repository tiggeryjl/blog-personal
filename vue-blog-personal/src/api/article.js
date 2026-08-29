import request from '@/utils/request';

//获取文章列表
export const getArticleListApi = () => request.get(`/user/article/getArticleList`);

//根据文章id获取文章详情
export const getArticleDetailApi = (id) => request.get(`/user/article/getArticleDetail/${id}`);
