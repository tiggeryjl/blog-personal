import request from '@/utils/request';

// 分页查询友链列表
export const getLinkPageApi = (data) =>
  request.get(`/admin/link/page`, { params: data });

// 统计各审核状态数量
export const getLinkStatsApi = () => request.get(`/admin/link/stats`);

// 根据ID查询友链
export const getLinkByIdApi = (id) => request.get(`/admin/link/${id}`);

// 新增友链
export const addLinkApi = (data) => request.post('/admin/link/add', data);

// 编辑友链
export const updateLinkApi = (data) => request.put('/admin/link/update', data);

// 审核友链（通过/拒绝）
export const auditLinkApi = (data) => request.put('/admin/link/audit', data);

// 启用/禁用友链
export const updateLinkStatusApi = (id, status) =>
  request.put(`/admin/link/${id}/status/${status}`);

// 批量逻辑删除友链
export const deleteLinkApi = (ids) =>
  request.delete(`/admin/link?ids=${ids}`);
