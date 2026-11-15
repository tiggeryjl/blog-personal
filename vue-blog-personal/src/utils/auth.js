import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/userloginstatus';

// 是否已登录
export const isLoggedIn = () => !!useUserStore().user_token;

/**
 * 需要登录才能进行的操作统一入口
 * 未登录时弹提示
 * @param {string} tip 未登录时的提示文案
 * @returns {boolean} true 表示已登录，可以继续执行操作
 */
export const requireLogin = async (tip = '请先登录后再进行该操作') => {
  const store = useUserStore();
  if (store.user_token) return true;

  // 首次判断时可能 cookie 还没换成 token
  if (!store.sessionChecked) {
    try {
      await store.restoreFromCookie(); // ← 见下面第 2 条，把 router 里的恢复逻辑挪到 store
    } catch (e) {
      /* 未登录，忽略 */
    }
    store.sessionChecked = true;
  }

  if (useUserStore().user_token) return true;
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
