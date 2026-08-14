import request from '@/utils/request';

//统计未读数和最新5条通知
export const getInitUnreadApi = () => request.get(`/admin/notice/initUnread`);

//分页查询通知信息
export const getNoticeListApi = (params) => request.get('/admin/notice/list', { params });

// 标记单条已读
export const markReadSingleApi = (id) => request.put(`/admin/notice/read/${id}`);

// 一键全部标记已读
export const markReadAllApi = () => request.put('/admin/notice/readAll');
