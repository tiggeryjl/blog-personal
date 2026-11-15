import { defineStore } from 'pinia';
import { ElMessage } from 'element-plus';

const emptyUserInfo = () => ({
  id: '',
  nickname: '',
  avatar: '',
  email: '',
  phone: '',
});
// 第一个参数 'user' 是唯一标识，可自定义
// 第二个参数是配置
export const useUserStore = defineStore('user', {
  state: () => ({
    user_token: '', // 用户 token
    userInfo: emptyUserInfo(),
    sessionChecked: false, // 本次页面生命周期内是否已经用 cookie 试过恢复登录态
  }),

  actions: {
    // 登录成功保存信息
    loginSuccess(res) {
      this.user_token = res.token;
      this.userInfo = {
        id: res.user.id,
        nickname: res.user.nickname,
        avatar: res.user.avatar,
        email: res.user.email,
        phone: res.user.phone,
      };

      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
      this.sessionChecked = true;
    },

    // 退出登录
    logout() {
      this.user_token = '';
      this.userInfo = emptyUserInfo();
      this.sessionChecked = true;
      localStorage.removeItem('userInfo');
    },

    async restoreFromCookie() {
      const { getRefreshTokenApi, queryUserInfoApi } = await import('@/api/auth.js'); // 动态引入避免循环依赖
      const res = await getRefreshTokenApi({ silent: true });
      const newToken = res?.code === 200 ? res.data?.token : null;
      if (!newToken) throw new Error('未登录');
      this.user_token = newToken;
      const info = await queryUserInfoApi();
      if (info?.data) this.updateUserInfo(info.data);
      this.sessionChecked = true;
      return true;
    },

    // 更新本地 token（静默刷新、登录过期清理时使用）
    setToken(token) {
      this.user_token = token || '';
    },

    // 刷新页面时，从本地恢复数据
    loadStorage() {
      try {
        const userInfoStr = localStorage.getItem('userInfo') || '';

        if (userInfoStr) {
          const info = JSON.parse(userInfoStr);
          this.userInfo = {
            id: info.id ?? '',
            nickname: info.nickname ?? '',
            avatar: info.avatar ?? '',
            email: info.email ?? '',
            phone: info.phone ?? '',
          };
        }
      } catch (e) {
        ElMessage.error('登录状态已失效，请重新登录!');
        this.logout();
      }
    },
    updateUserInfo(newInfo) {
      this.userInfo = {
        id: newInfo.id ?? this.userInfo.id,
        nickname: newInfo.nickname ?? this.userInfo.nickname,
        avatar: newInfo.avatar ?? this.userInfo.avatar,
        email: newInfo.email ?? this.userInfo.email,
        phone: newInfo.phone ?? this.userInfo.phone,
      };
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
    },
  },
});
