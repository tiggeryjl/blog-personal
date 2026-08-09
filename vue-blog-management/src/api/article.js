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

//修改文章状态
export const updateArticleStatusApi = (article) =>
  request.put("/admin/article/status", article);

//置顶文章
export const updateArticleTopApi = (id) => request.put(`/admin/article/${id}`);
