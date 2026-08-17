import request from "@/utils/request";

// 分页查询回收站日常
export const getRecycleDailyListApi = (data) =>
  request.get(`/admin/daily/recycleList`, { params: data });

// 恢复回收站日常（回收站 -> 正常列表）
export const recoverDailyApi = (ids) =>
  request.put(`/admin/daily/recover?ids=${ids}`);

// 彻底删除回收站日常
export const recycleDeleteDailyApi = (ids) =>
  request.delete(`/admin/daily/recycleDelete?ids=${ids}`);
