import request from '@/utils/request';


// 获取文章信息
export const getArticleListApi = (data) => request.get(`/admin/article/getArticleList`, { params: data });

//新增
export const addArticleApi = (article) => request.post('/admin/article/add', article);

//获取标签下拉选项
// export const getTagOptionsApi = () => request.get('/article/TagOptions');

//根据ID查询文章
export const getArticleDetailApi = (id) => request.get(`/admin/article/${id}`);

//修改
export const updateArticleApi = (article) => request.put('/admin/article/update', article);

//定时发布
export const setTimedApi = (article) => request.post('/admin/article/setTimed', article);

//取消定时
export const cancelTimedApi = (id) => request.post(`/admin/article/cancelTimed/${id}`);

//删除
// export const deleteTagApi = (ids) => request.delete(`/tags?ids=${ids}`);