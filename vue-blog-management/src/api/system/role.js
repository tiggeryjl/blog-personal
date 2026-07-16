import request from '@/utils/request';

//查询角色
export const getRoleListApi = (data) => request.get('/admin/role/getRoleList', { params: data });

//新增角色
export const addRoleApi = (data) => request.post('/admin/role/add', data);

//修改角色
export const updateRoleApi = (data) => request.put('/admin/role/update', data);

//逻辑删除角色
export const deleteRoleApi = (id) => request.delete(`/admin/role/logicDelete?id=${id}`);

//彻底删除角色
// export const deleteRoleApi = (id) => request.delete(`/admin/role?id=${id}`);

// 获取角色权限树
export const getRoleMenuTreeApi = (id) => request.get(`/admin/role/menuTreeSelect/${id}`);

//分配权限
export const assignRoleMenuApi = (data) => request.put('/admin/role/assignPermission', data);