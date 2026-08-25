import request from '@/utils/request'

// 查询已通过并启用的友链列表
export const getPublicLinkListApi = () => request.get('/link/list')

// 提交友链申请
export const applyLinkApi = (data) => request.post('/link/apply', data)

// 查询全部待审核/已拒绝的友链申请（按申请时间倒序）
export const getApplicationsApi = () => request.get('/link/applications')

// 催促审核本人的友链申请（需登录）
export const urgeLinkApi = (id) => request.post(`/link/urge/${id}`)
