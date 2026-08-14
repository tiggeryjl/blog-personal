import request from '@/utils/request';

// 登录
export const loginApi = (data) => request.post('/admin/admin/login', data);

// 新增用户
export const addUserApi = (data) => request.post('/admin/admin/add', data);

//查询用户及角色权限信息
export const getUserInfoApi = () => request.get('/admin/admin/getUserInfo');

//查询用户
export const getUserListApi = (data) => request.get('/admin/admin/getUserList', { params: data });

//修改
export const updateUserApi = (data) => request.put('/admin/admin/update', data);

//修改启用禁用状态
export const updateUserStatusApi = (id, status) => request.put(`/admin/admin/${id}/status/${status}`);

//获取用户角色集合
export const getRoleAllApi = () => request.get('/admin/admin/getRoleList');

//分配用户角色
export const changeRoleApi = (data) => request.put('/admin/admin/updateRole', data);

//修改密码
export const editPwdApi = (user) => request.put('/admin/admin/editPassword', user);

//删除
export const deleteUserApi = (ids) => request.delete(`/admin/admin?ids=${ids}`);

//逻辑删除
export const logicDeleteUserApi = (ids) => request.delete(`/admin/admin/logicDelete?ids=${ids}`);

//分页查询逻辑删除的用户
export const getLogicDeleteUserApi = (data) => request.get('/admin/admin/getLogicDelete', { params: data });

//恢复用户
export const recoverUserApi = (id) => request.put(`/admin/admin/recover?id=${id}`);

// 刷新token
export const getRefreshTokenApi = () => request.post(`/admin/admin/refreshToken`);

// 退出登录
export const logoutApi = () => request.post(`/admin/admin/logout`); 
