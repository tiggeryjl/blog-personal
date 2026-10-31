import request from '@/utils/request'

// 文件上传
export const uploadApi = (file) => request.post('/upload/upload', file)

// 用户头像上传
export const uploadAvatarApi = (file) => request.post('/upload/avatar', file)
