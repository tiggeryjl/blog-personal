import request from '@/utils/request';

// 分页查询音乐
export const getMusicListApi = (params) =>
  request.get('/admin/music/page', { params });

// 根据 ID 查询音乐
export const getMusicDetailApi = (id) => request.get(`/admin/music/${id}`);

// 新增音乐
export const addMusicApi = (data) => request.post('/admin/music/add', data);

// 编辑音乐
export const updateMusicApi = (data) => request.put('/admin/music/update', data);

// 修改上下架状态
export const updateMusicStatusApi = (id, status) =>
  request.put(`/admin/music/${id}/status/${status}`);

// 批量删除音乐
export const deleteMusicApi = (ids) =>
  request.delete('/admin/music', {
    params: { ids: Array.isArray(ids) ? ids.join(',') : ids },
  });
