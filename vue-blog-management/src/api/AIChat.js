import request from '@/utils/request';


// 获取文章信息
export const getAiChatApi = (message) => request.get(`/AiChat/ai/chat`, { params: { message } });