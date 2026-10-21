import request from '@/utils/request';

// 分页查询操作日志
export const getOperLogPageApi = (params) => request.get('/admin/operlog/page', { params });

// 查询日志详情
export const getOperLogDetailApi = (id) => request.get(`/admin/operlog/${id}`);

// 批量删除日志
export const deleteOperLogApi = (ids) => request.delete(`/admin/operlog?ids=${ids.join(',')}`);

// 清空全部日志（后端会再校验超管角色）
export const clearOperLogApi = () => request.delete('/admin/operlog/clear');
