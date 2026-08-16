import request from "@/utils/request";

// 分页查询文章评论
export const getArticleCommentListApi = (data) =>
  request.get(`/admin/comment/article/list`, { params: data });

// 分页查询日常评论
export const getDailyCommentListApi = (data) =>
  request.get(`/admin/comment/daily/list`, { params: data });

// 分页查询留言评论
export const getMessageCommentListApi = (data) =>
  request.get(`/admin/comment/message/list`, { params: data });

// 审核/隐藏评论
export const updateCommentStatusApi = (data) =>
  request.put("/admin/comment/status", data);

// 后台回复评论
export const addCommentReplyApi = (data) =>
  request.post("/admin/comment/reply", data);

// 置顶/取消置顶评论
export const updateCommentTopApi = (id) =>
  request.put(`/admin/comment/top/${id}`);

// 批量逻辑删除评论
export const logicDeleteCommentApi = (ids) =>
  request.delete(`/admin/comment/logicDelete?ids=${ids}`);
