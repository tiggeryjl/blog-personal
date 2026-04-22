<script setup>
import { ref } from 'vue'
import LoginPage from '@/components/login/LoginPage.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/userloginstatus'
import { ElMessage } from 'element-plus';

const userStore = useUserStore()
let loginForm = ref({ token: '', nickname: '', avatar: '', email: '', phone: '', password: '' })

const loginRef = ref();

const router = useRouter()

async function handleLogin(payload) {
  loginRef.value?.setLoading(true)

  // Replace with your real auth logic
  await new Promise(r => setTimeout(r, 500))

  if (payload.email === '2087691050@qq.com' && payload.password === '1234') {
    loginForm.value.token = 'eyJhbGciOiJIUzI1NiJ9.eyJpZCI6NDAsInVzZXJuYW1lIjoieWVzaXIiLCJleHAiOjE3NzIyMjk5NDl9.mKTizrokyQkL6xH9FXjSWbEfmGBKxL34Kii7v1oFURM'
    loginForm.value.nickname = '小叶同学'
    loginForm.value.email = '2087691050@qq.com'
    loginForm.value.password = '1234'
    loginForm.value.avatar = 'https://picsum.photos/64/64'
    userStore.loginSuccess(loginForm.value)
    // router.push('/index')
    router.go(-1)
    ElMessage.success("登录成功!");
  } else {
    loginRef.value?.setError('邮箱或密码错误')
  }

  loginRef.value?.setLoading(false)
}

async function handleRegister(payload) {

  loginRef.value?.setLoading(true)

  console.log("注册中。。。")
  await new Promise(r => setTimeout(r, 500))

  loginRef.value?.setLoading(false)
}
</script>

<template>
  <div class="login-wrapper">
    <LoginPage ref="loginRef" brand-name="您好，欢迎您的到来!" title="您好，欢迎你!" subtitle="请输入您的信息" primary-color="#4f46e5"
      @login-submit="handleLogin" @register-submit="handleRegister" />
  </div>
</template>

<style scoped>
.login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  /* 水平居中 */
  align-items: center;
  /* 垂直居中 */
  background: var(--bg-color);
  /* 柔和背景 */
  margin: 0;
  /* padding: 100px 100px; */
  box-sizing: border-box;
}

:deep(.page) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {

  :deep(.page) {
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
  }

  /* 如果 LoginPage 内部有 form 容器，也覆盖 */
  :deep(.form-container),
  :deep(.login-form),
  :deep(form) {
    width: 100%;
    max-width: 100%;
    padding: 0 16px;
    box-sizing: border-box;
  }
}

@media (min-width: 769px) and (max-width: 1023px) {

  :deep(.page) {
    width: 90% !important;
    /* 让表单宽度适中，不要太宽 */
    /* max-width: 600px !important; */
    /* 限制最大宽度，避免过宽 */
    margin: 0 auto !important;
  }

  /* 如果内部有双栏布局（左动画右表单），确保比例协调 */
  :deep(.left-panel),
  :deep(.right-panel) {
    flex: 1;
  }

  :deep(.right-panel) {
    padding: 40px !important;
  }
}
</style>