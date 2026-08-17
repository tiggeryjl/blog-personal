import request from '@/utils/request';

// 查询所有标签信息
export const findAllTagApi = () => request.get(`/admin/tags`);

//新增
export const addTagApi = (tag) => request.post('/admin/tags/add', tag);

//根据ID查询分类
export const queryInfoTagApi = (id) => request.get(`/admin/tags/${id}`);

//修改
export const updateTagApi = (tag) => request.put('/admin/tags/update', tag);

//修改启用禁用状态
export const updateTagStatusApi = (id, status) => request.put(`/admin/tags/${id}/status/${status}`);

//删除
export const deleteTagApi = (ids) => request.delete(`/admin/tags?ids=${ids}`);

//分页查询回收站标签
export const getRecycleTagListApi = (data) =>
  request.get(`/admin/tags/recycleList`, { params: data });

//恢复回收站标签（回收站 -> 正常列表）
export const recoverTagApi = (ids) =>
  request.put(`/admin/tags/recover?ids=${ids}`);

//彻底删除回收站标签
export const recycleDeleteTagApi = (ids) =>
  request.delete(`/admin/tags/recycleDelete?ids=${ids}`);


//获取标签下拉选项
export const getTagOptionsApi = () => request.get('/admin/tags/tagsOptions');
