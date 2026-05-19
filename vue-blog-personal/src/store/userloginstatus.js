import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus';

// 第一个参数 'user' 是唯一标识，可自定义
// 第二个参数是配置
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',         // 用户 token
    userInfo: {
      id: '',         // 用户 id
      nickname: '',      // 昵称
      avatar: '',        // 头像
      email: '',        // 邮箱
      phone: '',         // 手机号
    }
  }),

  actions: {
    // 登录成功保存信息
    loginSuccess(res) {

      this.token = res.token
      this.userInfo = {
        id: res.id,
        nickname: res.nickname,
        avatar: res.avatar,
        email: res.email,
        phone: res.phone
      };

      // 同时保存到 localStorage（刷新不丢失）
      localStorage.setItem('token', res.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    // 退出登录
    logout() {
      this.token = '';
      this.userInfo = {
        id: '',
        nickname: '',
        avatar: '',
        email: '',
        phone: ''
      };

      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 刷新页面时，从本地恢复数据
    loadStorage() {
      try {

        const token = localStorage.getItem('token') || ''
        const userInfoStr = localStorage.getItem('userInfo') || ''

        if (!token) {
          this.logout()
          return
        }

        this.token = token;
        if (userInfoStr) {
          const info = JSON.parse(userInfoStr)
          this.userInfo = {
            id: info.id || '',
            nickname: info.nickname || '',
            avatar: info.avatar || '',
            email: info.email || '',
            phone: info.phone || ''
          }
        }

      } catch (e) {
        ElMessage.error("登录状态已失效，请重新登录!");
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
      }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
    }
  }
})