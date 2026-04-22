import axios from 'axios'
import { ElMessage } from 'element-plus';
import router from '../router';
//调用路由函数返回路由实例

//创建axios实例对象
const request = axios.create({
  // baseURL: 'https://m1.apifoxmock.com/m1/6628842-6336405-default',
  baseURL: '/api',
  timeout: 600000 //600秒
})

//axios的请求 request 拦截器 - 获取localStorage中的token,在请求头中添加token
request.interceptors.request.use(
  (config) => {//成功回调
    const loginUser = JSON.parse(localStorage.getItem('userInfo'));
    if (loginUser && loginUser.token) {
      config.headers.token = loginUser.token;
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
    return response.data
  },
  (error) => { //失败回调
    if (error.response.status == 401) { //===:全等 两边都是同类型比如字符串类型才想等  ==:一边是字符串一边是整形也相等
      //提示信息
      ElMessage.error("登录超时请求登录");
      //跳转到登录页面
      router.push('/login');
    } else {
      ElMessage.error("接口访问异常");
    }
    return Promise.reject(error)
  }
)

export default request