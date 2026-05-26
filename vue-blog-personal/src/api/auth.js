import request from '@/utils/request';

// 用户登录
export const loginApi = (user) => request.post('/user/login', user);

//用户注册
export const registerApi = (user) => request.post('/user/register', user);

//根据ID查询用户信息
export const queryUserInfoApi = () => request.get(`/user/getUserInfo`);

//修改
export const updateApi = (user) => request.put('/user/update', user);

//修改密码
export const editPwdApi = (user) => request.put('/user/editPassword', user);
