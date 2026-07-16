import request from '@/utils/request';

//获取权限树
export const getMenuTree = () => request.get('/admin/menu/getMenuTree');

//查询逻辑删除的权限树
export const getdeleteMenuTree = () => request.get('/admin/menu/getLogicDelete');

//新增权限
export const addMenuApi = (data) => request.post('/admin/menu/add', data);

//修改权限
export const updateMenuApi = (data) => request.put('/admin/menu/update', data);

//逻辑删除权限
export const logicDeleteMenuApi = (id) => request.delete(`/admin/menu/logicDelete?id=${id}`);

//恢复删除权限
export const recoverMenuApi = (id) => request.put(`/admin/menu/recover?id=${id}`);

//彻底删除权限
export const deleteMenuApi = (id) => request.delete(`/admin/menu?id=${id}`);