import request from '@/utils/request';

// 查询所有分类信息
export const findAllApi = () => request.get(`/admin/categorys`);

//新增
export const addApi = (category) => request.post('/admin/categorys/add', category);

//根据ID查询分类
export const queryInfoApi = (id) => request.get(`/admin/categorys/${id}`);

//修改
export const updateApi = (category) => request.put('/admin/categorys/update', category);

//修改启用禁用状态
export const updateStatusApi = (id, status) => request.put(`/admin/categorys/${id}/status/${status}`);

//删除
export const deleteCategoryApi = (ids) => request.delete(`/admin/categorys?ids=${ids}`);

// 获取分类下拉选项
export const getCategoryOptionsApi = () => request.get(`/admin/categorys/categoryOptions`);