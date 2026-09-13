import { ElMessage } from 'element-plus';

// 是否已登录（以本地 token 为准）
export const isLoggedIn = () => !!localStorage.getItem('user_token');

/**
 * 需要登录才能进行的操作统一入口（评论、回复、点赞等）
 * 未登录时弹提示
 *
 * @param {string} tip 未登录时的提示文案
 * @returns {boolean} true 表示已登录，可以继续执行操作
 */
export const requireLogin = (tip = '请先登录后再进行该操作') => {
  if (isLoggedIn()) return true;
  ElMessage.warning(tip);
  return false;
};

export default { isLoggedIn, requireLogin };
