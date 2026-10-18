import request from '@/utils/request';

// 用户登录
export const loginApi = (user) => request.post('/user/user/login', user);

//用户注册
export const registerApi = (user) => request.post('/user/user/register', user);

// 判断邮箱是否已注册（留言板简约登录/注册）
export const emailExistsApi = (email) => request.get('/user/user/emailExists', { params: { email } });

// 邮箱快捷注册（留言板简约注册，仅需邮箱与密码）
export const registerByEmailApi = (data) => request.post('/user/user/registerByEmail', data);

//根据ID查询用户信息
export const queryUserInfoApi = () => request.get(`/user/user/getUserInfo`);

//修改
export const updateApi = (user) => request.put('/user/user/update', user);

//修改密码
export const editPwdApi = (user) => request.put('/user/user/editPassword', user);

// 忘记密码：发送邮箱验证码
export const sendResetPwdCodeApi = (data) => request.post('/user/user/resetPwd/code', data);

// 忘记密码：校验邮箱验证码，通过后返回一次性凭证
export const verifyResetPwdCodeApi = (data) => request.post('/user/user/resetPwd/verify', data);

// 忘记密码：凭一次性凭证重置密码
export const resetPwdByTicketApi = (data) => request.post('/user/user/resetPwd', data);

// 刷新token
export const getRefreshTokenApi = (config) => request.post(`/user/user/refreshToken`, null, config);

// 退出登录（通知后端清除 HttpOnly 的 refreshToken cookie）
export const logoutApi = () => request.post('/user/user/logout');

// 获取个人信息
export const getPersonalInfoApi = () => request.get(`/user/user/personalInfo`);
