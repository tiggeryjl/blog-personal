import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getRefreshTokenApi } from '@/api/auth.js'
import { useUserStore } from '@/store/userloginstatus'
//调用路由函数返回路由实例

// 并发刷新锁 + 请求等待队列
let isRefreshing = false
let waitRequestQueue = []

//创建axios实例对象
const request = axios.create({
  // baseURL: 'https://m1.apifoxmock.com/m1/6628842-6336405-default',
  baseURL: '/api',
  timeout: 600000, //600秒
  withCredentials: true,
})

//axios的请求 request 拦截器 - 获取localStorage中的token,在请求头中添加token
request.interceptors.request.use(
  (config) => {
    //成功回调
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      config.headers.token = token
    }
    return config
  },
  (error) => {
    //失败回调
    return Promise.reject(error)
  }
)

// axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => {
    //成功回调
    const { data, status } = response
    if (status === 200) {
      return data
    } else {
      ElMessage.error('请求失败')
      return Promise.reject(response)
    }
  },
  async (error) => {
    //失败回调
    const originalReq = error.config || {}

    // 静默请求（例如进入页面时悄悄尝试恢复登录态）失败时不提示、不跳转
    // _skipAuthRefresh：本次请求自身就是刷新token，失败后不能再触发刷新
    if (originalReq.silent || originalReq._skipAuthRefresh) {
      return Promise.reject(error)
    }

    // 仅处理401且未重试过的接口
    if (error.response?.status === 401 && !originalReq._retry) {
      // 游客本来就没有登录态，说明这个接口需要登录，直接提示，不再尝试刷新token
      if (!localStorage.getItem('user_token')) {
        originalReq._retry = true
        ElMessage.warning('请先登录后再进行该操作')
        return Promise.reject(error)
      }

      // 正在刷新，加入等待队列
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          waitRequestQueue.push({
            resolve: (newToken) => {
              originalReq.headers.Authorization = `Bearer ${newToken}`
              resolve(request(originalReq))
            },
            reject,
          })
        })
      }

      isRefreshing = true
      originalReq._retry = true

      try {
        // 调用用户端刷新token接口，自动携带Cookie里的refreshToken
        const res = await getRefreshTokenApi({ _skipAuthRefresh: true })
        const newToken = res?.code === 200 ? res.data?.token : null
        if (!newToken) throw new Error(res?.msg || '刷新token失败')
        // 更新本地存储新token
        useUserStore().setToken(newToken)
        // 执行队列所有等待接口
        waitRequestQueue.forEach((item) => item.resolve(newToken))
        waitRequestQueue = []
        // 重试当前报错接口
        return request(originalReq)
      } catch (refreshErr) {
        // 刷新失败，清空本地登录态
        useUserStore().setToken('')
        sessionStorage.clear()
        // 队列里等待的接口统一失败，避免请求一直挂起
        waitRequestQueue.forEach((item) => item.reject(refreshErr))
        waitRequestQueue = []
        ElMessage.error('登录已过期，请重新登录')
        // 只有当前页面本身需要登录时才强制跳登录页，浏览公开内容时不打断用户
        if (router.currentRoute.value.meta?.requiresAuth) {
          router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
        }
        // 公开的 GET 接口（文章、评论列表等）不带 token 重新请求一次，登录态失效也能继续浏览
        if ((originalReq.method || 'get').toLowerCase() === 'get') {
          delete originalReq.headers.Authorization
          delete originalReq.headers.token
          originalReq._retry = true
          return request(originalReq)
        }
        return Promise.reject(refreshErr)
      } finally {
        isRefreshing = false
      }
    }
    // 非401错误统一提示
    ElMessage.error('接口访问异常')
    return Promise.reject(error)
  }
)

export default request
