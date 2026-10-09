import request from '@/utils/request';

// 分页查询文章评论
export const getArticleCommentListApi = (id) => request.get(`/user/comment/article/${id}`);

// 发表文章顶级评论
export const addArticleCommentApi = (articleId, content) =>
  request.post(`/user/comment/article/${articleId}`, { content });

// 根据日常ID查询评论
export const getDailyCommentListApi = (id) => request.get(`/user/comment/daily/${id}`);

// 发表日常顶级评论
export const addDailyCommentApi = (dailyId, content) => request.post(`/user/comment/daily/${dailyId}`, { content });

// 分页查询留言评论
export const getMessageCommentListApi = (data) => request.get(`/user/comment/message/list`, { params: data });

// 发表留言
export const addMessageCommentApi = (data) => request.post('/user/comment/message', data);

// 回复评论
export const addCommentReplyApi = (data) => request.post('/user/comment/reply', data);
