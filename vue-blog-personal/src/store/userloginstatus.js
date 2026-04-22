import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus';

// 第一个参数 'user' 是唯一标识，可自定义
// 第二个参数是配置
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',         // 用户 token
    nickname: '',      // 昵称
    avatar: '',        // 头像
    email: '',        // 邮箱
    phone: '',         // 手机号
    password: ''    // 密码
  }),

  actions: {
    // 登录成功保存信息
    loginSuccess(userInfo) {
      this.token = userInfo.token
      this.nickname = userInfo.nickname
      this.avatar = userInfo.avatar
      this.email = userInfo.email
      this.phone = userInfo.phone
      this.password = userInfo.password

      // 同时保存到 localStorage（刷新不丢失）
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    // 退出登录
    logout() {
      this.token = ''
      this.nickname = ''
      this.avatar = ''
      this.email = ''
      this.phone = ''
      this.password = ''

      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 刷新页面时，从本地恢复数据
    loadStorage() {
      try {

        const token = localStorage.getItem('token') || ''
        const userInfo = localStorage.getItem('userInfo') || ''

        if (token && userInfo) {
          const info = JSON.parse(userInfo)
          this.token = info.token
          this.nickname = info.nickname
          this.avatar = info.avatar
          this.email = info.email
          this.phone = info.phone
          this.password = info.password
        }
      } catch {
        ElMessage.error("您未登录!");
      }

    }
  }
})