import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/userloginstatus';
import { getRefreshTokenApi } from '@/api/admin.js';
import router from '../router';
//调用路由函数返回路由实例

//创建axios实例对象
const request = axios.create({
  // baseURL: 'https://m1.apifoxmock.com/m1/6628842-6336405-default',
  baseURL: '/api',
  timeout: 600000, //600秒
  withCredentials: true,
});

//axios的请求 request 拦截器 - 获取localStorage中的token,在请求头中添加token
request.interceptors.request.use(
  (config) => {
    //成功回调
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
      // config.headers.token = userStore.token
    }
    return config;
  },
  (error) => {
    //失败回调
    return Promise.reject(error);
  }
);

// 标记是否正在刷新token，防止多个接口并发重复刷新
let isRefreshing = false;
// 等待刷新的请求队列
let waitRequestQueue = [];

//axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => {
    //成功回调
    return response.data;
  },
  async (error) => {
    //失败回调
    const userStore = useUserStore();
    const originalReq = error.config;

    if (error.response?.status == 401 && !originalReq._retry) {
      if (isRefreshing) {
        // 正在刷新，把当前接口放入队列，等刷新完重试
        return new Promise((resolve) => {
          waitRequestQueue.push((token) => {
            originalReq.headers.Authorization = `Bearer ${token}`;
            resolve(request(originalReq));
          });
        });
      }

      isRefreshing = true;
      originalReq._retry = true;

      try {
        // 调用刷新token接口，自动带cookie里的refreshToke
        const result = await getRefreshTokenApi();
        const newToken = result.data.token;
        // 更新全局token
        userStore.setToken(newToken);
        // 执行队列里等待的接口
        waitRequestQueue.forEach((cb) => cb(newToken));
        waitRequestQueue = [];
        // 重试当前报错接口
        return request(originalReq);
        // return service(originalReq)
      } catch (refreshErr) {
        // refreshToken失效/过期，彻底登出
        userStore.logout();
        localStorage.clear();
        sessionStorage.clear();
        ElMessage.error('登录超时请重新登录');
        router.push('/login');
        waitRequestQueue = [];
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    } else {
      if (!originalReq.url.includes('refreshToken')) {
        ElMessage.error('接口访问异常!');
      }
    }
    return Promise.reject(error);
  }
);

export default request;
