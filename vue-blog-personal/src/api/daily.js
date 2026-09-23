import request from '@/utils/request';

// 分页查询用户端日常列表
export const getDailyListApi = (params) => request.get('/user/daily/list', { params });

// 根据ID查询用户端日常详情
export const getDailyDetailApi = (id) => request.get(`/user/daily/getDailyDetail/${id}`);

// 日常浏览数 +1
export const addDailyViewApi = (id) => request.put(`/user/daily/${id}/view`);
