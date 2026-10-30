<script setup>
import { ref } from 'vue'
import LoginPage from '@/components/login/LoginPage.vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userloginstatus'
import { ElMessage } from 'element-plus';
import { loginApi, registerApi, verifyLoginCodeApi } from '@/api/auth'
import { saveRememberedAccount } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const loginRef = ref();
const userStore = useUserStore()
let pendingLogin = null

const finishLogin = (data, payload) => {
  if (!data?.token) {
    loginRef.value?.setError('登录凭证异常，请重新登录')
    return
  }

  saveRememberedAccount(payload.remember ? payload.account : '')
  ElMessage.success('登录成功!')
  userStore.loginSuccess(data)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/index'
  router.replace(redirect)
}

const handleLogin = async (payload) => {

  try {
    loginRef.value?.setLoading(true)

    const result = await loginApi({
      loginName: payload.account,
      password: payload.password,
      remember: payload.remember,
      deviceId: payload.deviceId,
      captchaVerifyParam: payload.captchaVerifyParam,
    })

    if (result.code === 200) {
      if (result.data?.needEmailCode) {
        pendingLogin = payload
        loginRef.value?.requireLoginEmailCode(result.data)
        return
      }
      pendingLogin = null
      finishLogin(result.data, payload)
    } else {
      loginRef.value?.setError(result.msg)
    }
  } catch (error) {
    console.error('登录异常：', error);
    loginRef.value?.setError(error?.response?.data?.msg || '登录失败，请稍后重试')
  } finally {
    loginRef.value?.setLoading(false)
  }
}

const handleLoginVerify = async (payload) => {
  if (!pendingLogin) {
    loginRef.value?.setError('登录验证已失效，请重新输入账号密码')
    return
  }

  try {
    loginRef.value?.setLoading(true)
    const result = await verifyLoginCodeApi({
      riskTicket: payload.riskTicket,
      emailCode: payload.emailCode,
      deviceId: payload.deviceId,
      remember: payload.remember,
    })

    if (result.code === 200) {
      const loginPayload = pendingLogin
      pendingLogin = null
      finishLogin(result.data, loginPayload)
    } else {
      loginRef.value?.setError(result.msg)
    }
  } catch (error) {
    console.error('登录验证码校验异常：', error)
    loginRef.value?.setError(error?.response?.data?.msg || '邮箱验证失败，请稍后重试')
  } finally {
    loginRef.value?.setLoading(false)
  }
}

const handleRegister = async (payload) => {
  try {
    loginRef.value?.setLoading(true)

    const result = await registerApi({
      nickname: payload.nickname,
      username: payload.username,
      phone: payload.phone,
      email: payload.email,
      password: payload.password,
      confirmPwd: payload.confirmPwd,
      emailCode: payload.emailCode,
      deviceId: payload.deviceId
    })

    if (result.code === 200) {
      ElMessage.success("注册成功，请登录!");
      loginRef.value?.registrationSucceeded()
    } else {
      loginRef.value?.setError(result.msg)
    }
  } catch (error) {
    console.error('注册异常：', error);
    loginRef.value?.setError(error?.response?.data?.msg || '注册失败，请稍后重试')
  } finally {
    loginRef.value?.setLoading(false)
  }
}
</script>

<template>
  <div class="login-wrapper">
    <LoginPage ref="loginRef" brand-name="您好，欢迎您的到来!" title="您好，欢迎你!" subtitle="请输入您的信息" primary-color="#4f46e5"
      @login-submit="handleLogin" @login-verify-submit="handleLoginVerify" @register-submit="handleRegister" />
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
  /* 登录/注册页固定浅色背景，不跟随站点主题 */
  background: #fff;
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
