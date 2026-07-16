import request from '@/utils/request';

//新增
export const addArticleApi = (article) => request.post('/admin/article/add', article);

// 获取分类下拉选项
// export const getCategoryOptionsApi = () => request.get(`/article/CategoryOptions`);

//获取标签下拉选项
// export const getTagOptionsApi = () => request.get('/article/TagOptions');

//根据ID查询分类
// export const queryInfoTagApi = (id) => request.get(`/tags/${id}`);

//修改
// export const updateTagApi = (tag) => request.put('/tags/update', tag);

//修改启用禁用状态
// export const updateTagStatusApi = (id, status) => request.put(`/tags/${id}/status/${status}`);

//删除
// export const deleteTagApi = (ids) => request.delete(`/tags?ids=${ids}`);