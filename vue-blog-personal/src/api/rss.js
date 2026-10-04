import request from '@/utils/request';

//邮箱订阅新文章通知
export const subscribeRssApi = (data) => request.post(`/user/rss/subscribe`, data);

//通过退订令牌取消订阅
export const unsubscribeRssApi = (token) => request.get(`/user/rss/unsubscribe`, { params: { token } });

//当前订阅人数
export const getRssCountApi = () => request.get(`/user/rss/count`);
