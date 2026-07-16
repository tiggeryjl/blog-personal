import request from '@/utils/request';

// 用户登录
export const loginApi = (user) => request.post('/user/user/login', user);

//用户注册
export const registerApi = (user) => request.post('/user/user/register', user);

//根据ID查询用户信息
export const queryUserInfoApi = () => request.get(`/user/user/getUserInfo`);

//修改
export const updateApi = (user) => request.put('/user/user/update', user);

//修改密码
export const editPwdApi = (user) => request.put('/user/user/editPassword', user);

// 刷新token
export const getRefreshTokenApi = () => request.post(`/user/user/refreshToken`);
