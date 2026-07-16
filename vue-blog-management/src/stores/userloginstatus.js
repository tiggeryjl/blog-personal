import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus';
import { logoutApi } from '@/api/admin'


export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',         // 用户 token
    userInfo: {
      id: '',         // 用户 id
      nickname: '',      // 昵称
      avatar: '',        // 头像
    }
  }),

  actions: {

    setToken(newToken) {
      this.token = newToken
      localStorage.setItem('token', newToken)
    },
    // 登录成功保存信息
    loginSuccess(res) {

      this.token = res.token
      this.userInfo = {
        id: res.user.id,
        nickname: res.user.nickname,
        avatar: res.user.avatar
      };

      localStorage.setItem('token', res.token)
    },

    // 退出登录
    async logout() {
      this.$reset()

      await logoutApi()

      localStorage.removeItem('user')
      localStorage.removeItem('token')
      sessionStorage.clear()
    },

    updateUserInfo(newInfo) {
      this.userInfo = {
        id: newInfo.id ?? this.userInfo.id,
        nickname: newInfo.nickname ?? this.userInfo.nickname,
        avatar: newInfo.avatar ?? this.userInfo.avatar
      }
    }
  },
  persist: {
    paths: ['token']
  } // 开启持久化存储localStorage
})