import request from "@/utils/request";

// 获取文章信息
export const getArticleListApi = (data) =>
  request.get(`/admin/article/getArticleList`, { params: data });

//新增
export const addArticleApi = (article) =>
  request.post("/admin/article/add", article);

//根据ID查询文章
export const getArticleDetailApi = (id) => request.get(`/admin/article/${id}`);

//修改
export const updateArticleApi = (article) =>
  request.put("/admin/article/update", article);

//定时发布
export const setTimedApi = (article) =>
  request.post("/admin/article/setTimed", article);

//取消定时
export const cancelTimedApi = (id) =>
  request.post(`/admin/article/cancelTimed/${id}`);

//逻辑删除文章
export const logicDeleteArticleApi = (ids) =>
  request.delete(`/admin/article/logicDelete?ids=${ids}`);

// 删除文章
export const deleteArticleApi = (ids) =>
  request.delete(`/admin/article?ids=${ids}`);

//分页查询回收站文章
export const getRecycleArticleListApi = (data) =>
  request.get(`/admin/article/recycleList`, { params: data });

//恢复回收站文章（回收站 -> 正常列表）
export const recoverArticleApi = (ids) =>
  request.put(`/admin/article/recover?ids=${ids}`);

//彻底删除回收站文章
export const recycleDeleteArticleApi = (ids) =>
  request.delete(`/admin/article/recycleDelete?ids=${ids}`);

//修改文章状态
export const updateArticleStatusApi = (article) =>
  request.put("/admin/article/status", article);

//置顶文章
export const updateArticleTopApi = (id) => request.put(`/admin/article/${id}`);
