import request from '@/utils/request';

//获取标签列表
export const getTagListApi = () => request.get(`/user/tags/tag`);
