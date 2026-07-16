import request from '@/utils/request';

// 文件上传
export const uploadApi = (file) => request.post('/upload/upload', file);
