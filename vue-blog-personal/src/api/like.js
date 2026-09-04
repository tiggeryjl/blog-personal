import request from '@/utils/request';

// 点赞目标类型，与后端 LikeConstant 保持一致
export const LIKE_TARGET_TYPE = {
  ARTICLE: 0,
  COMMENT: 2,
};

// 点赞文章/评论(后端按登录用户幂等去重)
export const likeApi = (data) => request.post('/user/like', data);
