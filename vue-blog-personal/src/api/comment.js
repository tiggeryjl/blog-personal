import request from '@/utils/request';

// 分页查询文章评论
export const getArticleCommentListApi = (id) => request.get(`/user/comment/article/${id}`);

// 发表文章顶级评论
export const addArticleCommentApi = (articleId, content) =>
  request.post(`/user/comment/article/${articleId}`, { content });

// 分页查询日常评论
export const getDailyCommentListApi = (data) => request.get(`/user/comment/daily/list`, { params: data });

// 分页查询留言评论
export const getMessageCommentListApi = (data) => request.get(`/user/comment/message/list`, { params: data });

// 回复评论
export const addCommentReplyApi = (data) => request.post('/user/comment/reply', data);
