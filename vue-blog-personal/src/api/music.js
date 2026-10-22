import request from '@/utils/request';

// 获取用户端已上架音乐
export const getMusicListApi = () => request.get('/user/music/list');
