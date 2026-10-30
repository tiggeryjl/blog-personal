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
const DEVICE_ID_KEY = 'blog_device_id';

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

/**
 * 获取当前浏览器稳定设备标识，不存在时自动生成
 *
 * @returns {string} 设备标识
 */
export const getOrCreateDeviceId = () => {
  let deviceId = localStorage.getItem(DEVICE_ID_KEY);
  if (deviceId) return deviceId;

  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    deviceId = crypto.randomUUID();
  } else {
    const randomPart = Math.random().toString(36).slice(2);
    deviceId = `${Date.now().toString(36)}-${randomPart}`;
  }
  localStorage.setItem(DEVICE_ID_KEY, deviceId);
  return deviceId;
};

export default {
  isLoggedIn,
  requireLogin,
  getRememberedAccount,
  saveRememberedAccount,
  getOrCreateDeviceId,
};
