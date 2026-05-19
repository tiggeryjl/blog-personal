import request from '@/utils/request';

// 文件上传
export const uploadApi = (file) => request.post('/upload', file);

//用户注册
// export const registerApi = (user) => request.post('/user/register', user);

//根据ID查询用户信息
// export const queryByIdApi = (id) => request.get(`/user/getUserInfo?id=${id}`);
// export const queryByIdApi = (id) => request.get(`/user/getUserInfo/${id}`);

//修改
// export const updateApi = (user) => request.put('/user/update', user);

// //删除
// export const deleteApi = (id) => request.delete(`/depts?id=${id}`);