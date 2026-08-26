import request from "@/utils/request";

// 分页查询日常列表
export const getDailyListApi = (data) =>
  request.get(`/admin/daily/list`, { params: data });

// 新增日常
export const addDailyApi = (data) =>
  request.post("/admin/daily/add", data);

// 根据ID查询日常
export const getDailyDetailApi = (id) =>
  request.get(`/admin/daily/${id}`);

// 修改日常
export const updateDailyApi = (data) =>
  request.put("/admin/daily/update", data);

// 修改日常状态（立即发布/下架/上架/私密等）
export const updateDailyStatusApi = (data) =>
  request.put("/admin/daily/status", data);

// 置顶切换
export const updateDailyTopApi = (id) =>
  request.put(`/admin/daily/${id}`);

// 设置定时发布
export const setDailyTimedApi = (data) =>
  request.post("/admin/daily/setTimed", data);

// 取消定时发布
export const cancelDailyTimedApi = (id) =>
  request.post(`/admin/daily/cancelTimed/${id}`);

// 批量逻辑删除日常（移入回收站）
export const logicDeleteDailyApi = (ids) =>
  request.delete(`/admin/daily/logicDelete?ids=${ids}`);

// 分页查询回收站日常
export const getRecycleDailyListApi = (data) =>
  request.get(`/admin/daily/recycleList`, { params: data });

// 恢复回收站日常（回收站 -> 正常列表）
export const recoverDailyApi = (ids) =>
  request.put(`/admin/daily/recover?ids=${ids}`);

// 彻底删除回收站日常
export const recycleDeleteDailyApi = (ids) =>
  request.delete(`/admin/daily/recycleDelete?ids=${ids}`);
