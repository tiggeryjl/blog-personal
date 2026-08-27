import request from '@/utils/request'

// 日常浏览数 +1
export const addDailyViewApi = (id) => request.put(`/user/daily/${id}/view`)
