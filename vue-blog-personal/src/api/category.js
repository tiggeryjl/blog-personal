import request from '@/utils/request';

//获取分类列表
export const getCategoryListApi = () => request.get(`/user/categorys/category`);
