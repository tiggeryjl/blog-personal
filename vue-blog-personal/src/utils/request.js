import axios from 'axios'
import { ElMessage } from 'element-plus';
import router from '../router';
import { getRefreshTokenApi } from '@/api/auth.js'
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
  (config) => {//成功回调
    const token = localStorage.getItem('user_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      config.headers.token = token;
    }
    return config;
  },
  (error) => {//失败回调
    return Promise.reject(error)
  }
)

// axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => { //成功回调
    const { data, status } = response
    if (status === 200) {
      return data
    } else {
      ElMessage.error('请求失败')
      return Promise.reject(response)
    }
  },
  async (error) => { //失败回调
    const originalReq = error.config
    // 仅处理401且未重试过的接口
    if (error.response?.status === 401 && !originalReq._retry) {
      // 正在刷新，加入等待队列
      if (isRefreshing) {
        return new Promise(resolve => {
          waitRequestQueue.push((newToken) => {
            originalReq.headers.Authorization = `Bearer ${newToken}`
            resolve(request(originalReq))
          })
        })
      }

      isRefreshing = true
      originalReq._retry = true

      try {
        // 调用用户端刷新token接口，自动携带Cookie里的refreshToken
        const res = await getRefreshTokenApi()
        const newToken = res.data.token
        // 更新本地存储新token
        localStorage.setItem('user_token', newToken)
        // 执行队列所有等待接口
        waitRequestQueue.forEach(cb => cb(newToken))
        waitRequestQueue = []
        // 重试当前报错接口
        return request(originalReq)
      } catch (refreshErr) {
        // 刷新失败，清空本地登录态跳登录
        localStorage.removeItem('user_token')
        sessionStorage.clear()
        waitRequestQueue = []
        ElMessage.error("登录超时，请重新登录");
        router.push('/login');
        return Promise.reject(refreshErr)
      } finally {
        isRefreshing = false
      }
    }
    // 非401错误统一提示
    ElMessage.error("接口访问异常");
    return Promise.reject(error)
  }
)

export default request