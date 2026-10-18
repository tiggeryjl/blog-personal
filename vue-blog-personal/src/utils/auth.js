import { ElMessage } from 'element-plus';

// 是否已登录
export const isLoggedIn = () => !!localStorage.getItem('user_token');

/**
 * 需要登录才能进行的操作统一入口
 * 未登录时弹提示
 * @param {string} tip 未登录时的提示文案
 * @returns {boolean} true 表示已登录，可以继续执行操作
 */
export const requireLogin = (tip = '请先登录后再进行该操作') => {
  if (isLoggedIn()) return true;
  ElMessage.warning(tip);
  return false;
};

const REMEMBERED_ACCOUNT_KEY = 'rememberedAccount';

/**
 * 读取上次勾选"记住我"时保存的账号
 */
export const getRememberedAccount = () => localStorage.getItem(REMEMBERED_ACCOUNT_KEY) || '';

/**
 * 保存账号，传空值表示取消勾选"记住我"，此时清除已保存的账号
 *
 * @param {string} account 账号
 */
export const saveRememberedAccount = (account) => {
  const value = (account || '').trim();
  if (value) {
    localStorage.setItem(REMEMBERED_ACCOUNT_KEY, value);
  } else {
    localStorage.removeItem(REMEMBERED_ACCOUNT_KEY);
  }
};

export default { isLoggedIn, requireLogin, getRememberedAccount, saveRememberedAccount };
