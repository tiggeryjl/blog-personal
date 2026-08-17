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

// 分页查询回收站文章评论
export const getRecycleArticleCommentListApi = (data) =>
  request.get(`/admin/comment/recycle/article/list`, { params: data });

// 分页查询回收站日常评论
export const getRecycleDailyCommentListApi = (data) =>
  request.get(`/admin/comment/recycle/daily/list`, { params: data });

// 分页查询回收站留言评论
export const getRecycleMessageCommentListApi = (data) =>
  request.get(`/admin/comment/recycle/message/list`, { params: data });

// 批量恢复评论（回收站 -> 正常列表）
export const recoverCommentApi = (ids) =>
  request.put(`/admin/comment/recover?ids=${ids}`);

// 彻底删除回收站评论
export const recycleDeleteCommentApi = (ids) =>
  request.delete(`/admin/comment/recycleDelete?ids=${ids}`);
