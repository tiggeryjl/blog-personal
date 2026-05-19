<script setup>
import { ref, computed, onMounted } from 'vue'
import { Eye, EyeOff, Mail, Sparkles } from 'lucide-vue-next'
import AnimatedCharacters from './AnimatedCharacters.vue'
import Captcha from '@/components/Captcha.vue';

const props = defineProps({
  brandName: {
    type: String,
    default: '您好'
  },
  title: {
    type: String,
    default: '您好，欢迎你!'
  },
  subtitle: {
    type: String,
    default: '请输入您的信息'
  },
  accountPlaceholder: {
    type: String,
    default: '请输入用户名/手机号/邮箱'
  },
  primaryColor: {
    type: String,
    default: '#4f46e5'
  },
  showGoogleLogin: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['login-submit', 'register-submit'])

const isLogin = ref(true)

// 登录表单
const account = ref('')
const password = ref('')
const remember = ref(false)

// 注册表单
const nickname = ref('')
const username = ref('')
const phone = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regConfirmPwd = ref('')
const regAgree = ref(false)

// 登录验证码
const needLoginCaptcha = ref(false)
const loginEmailCode = ref('')
const loginCodeBtnDisabled = ref(false)
const loginCodeBtnText = ref('获取验证码')
//注册验证码
const needEmailCode = ref(false)
const codeBtnDisabled = ref(false)
const codeBtnText = ref('获取验证码')
const emailCode = ref('')

// 展示密码动画状态
const showLoginPwd = ref(false)
const showRegPwd = ref(false)

// 状态
const errorMsg = ref('')
const loading = ref(false)
const isTyping = ref(false)

/** 为父级暴露以控制错误/加载状态 */
defineExpose({
  setError: (msg) => { errorMsg.value = msg },
  setLoading: (v) => { loading.value = v },
})

const currentPassword = computed(() => {
  return isLogin.value ? password.value : regPassword.value
})
const showPassword = computed(() => {
  return isLogin.value ? showLoginPwd.value : showRegPwd.value
})

//滑块验证码校验逻辑
const captchaComponent = ref(null)
let pendingLoginData = null

const handleCaptchaFail = () => {
  loading.value = false
  errorMsg.value = ''
  pendingLoginData = null
}

const handleCaptchaSuccess = (verifyParam) => {
  if (!pendingLoginData) {
    loading.value = false
    return
  }

  if (!verifyParam) {
    errorMsg.value = '验证参数无效，请重试'
    loading.value = false
    pendingLoginData = null
    return
  }

  loading.value = true
  errorMsg.value = ''

  emit('login-submit', {
    ...pendingLoginData,
    captchaVerifyParam: verifyParam
  })

  pendingLoginData = null
}

// 登录提交
const onSubmit = async () => {
  // 统一校验
  const valid = await validate('login')
  if (!valid) return

  pendingLoginData = {
    account: account.value,
    password: password.value,
    remember: remember.value,
    emailCode: loginEmailCode.value,
  }

  loading.value = true
  errorMsg.value = ''

  if (captchaComponent.value) {
    captchaComponent.value.show()        // 弹出滑块
  } else {
    errorMsg.value = '验证码组件未加载'
    loading.value = false
    pendingLoginData = null
  }

  // if (needLoginCaptcha.value) {
  //   emit('login-submit', {
  //     account: account.value,
  //     password: password.value,
  //     remember: remember.value,
  //     emailCode: loginEmailCode.value,
  //     ticket: localStorage.getItem('lastTicket'),
  //     randstr: localStorage.getItem('lastRandstr'),
  //   })
  //   return
  // }
}

// 登录滑块验证回调
const loginCaptchaBack = async (res) => {
  if (res.ret !== 0) {
    errorMsg.value = '验证失败'
    loading.value = false
    return
  }

  localStorage.setItem('lastTicket', res.ticket)
  localStorage.setItem('lastRandstr', res.randstr)

  // 发给后端判断是否风险登录
  const { data } = await axios.post('/api/login/check', {
    account: account.value,
    password: password.value,
    ticket: res.ticket,
    randstr: res.randstr
  })

  loading.value = false

  if (data.needEmailCode) {
    needLoginCaptcha.value = true
    errorMsg.value = '当前为风险登录，请完成邮箱验证'
  } else {
    // 无风险 → 直接登录成功
    emit('login-submit', {
      account: account.value,
      password: password.value,
      remember: remember.value
    })
  }
}

// 登录发送邮箱验证码
const sendLoginEmailCode = async () => {
  await axios.post('/api/login/sendCode', { account: account.value })
  loginCodeBtnDisabled.value = true
  loginCodeBtnText.value = '重新发送(60s)'
  setTimeout(() => {
    loginCodeBtnDisabled.value = false
    loginCodeBtnText.value = '获取验证码'
  }, 60000)
}

// 注册提交
const onRegister = async () => {
  const valid = await validate('register')
  if (!valid) return

  // captcha.show();
  loading.value = true
  errorMsg.value = '';

  emit('register-submit', {
    nickname: nickname.value,
    username: username.value,
    phone: phone.value,
    email: regEmail.value,
    password: regPassword.value,
    confirmPwd: regConfirmPwd.value,
    emailCode: emailCode.value
  })
  isLogin.value = true
}

onMounted(() => { })

const captchaBack = async (res) => {
  if (res.ret !== 0) return alert('滑块失败')

  localStorage.setItem('lastTicket', res.ticket)
  localStorage.setItem('lastRandstr', res.randstr)

  // 先检查是否需要邮箱验证
  const { data } = await axios.post('/api/register/check', {
    username: username.value,
    password: password.value,
    regEmail: regEmail.value,
    ticket: res.ticket,
    randstr: res.randstr
  })

  if (data.needEmailCode) {
    needEmailCode.value = true
    alert('当前为风险注册，需要邮箱验证')
  } else {
    alert('注册成功')
  }
}
// 发送邮箱验证码
const sendEmailCode = async () => {
  await axios.post('/api/register/sendCode', { account: account.value })
  // 倒计时逻辑自己加
}

// 确认注册（带邮箱验证码）
const confirmRegister = async () => {
  await axios.post('/api/register/confirm', {
    username: username.value,
    password: password.value,
    regEmail: regEmail.value,
    emailCode: emailCode.value,
    ticket: localStorage.getItem('lastTicket'),
    randstr: localStorage.getItem('lastRandstr')
  })
  alert('注册成功')
}

// 重置表单（切换登录/注册时调用）
const resetForm = () => {
  // 清空输入
  account.value = ''
  password.value = ''
  remember.value = false

  nickname.value = ''
  username.value = ''
  phone.value = ''
  regEmail.value = ''
  regPassword.value = ''
  regConfirmPwd.value = ''
  emailCode.value = ''
  loginEmailCode.value = ''

  // 清空验证状态
  needLoginCaptcha.value = false
  needEmailCode.value = false
  loginCodeBtnDisabled.value = false
  loginCodeBtnText.value = '获取验证码'
  codeBtnDisabled.value = false
  codeBtnText.value = '获取验证码'

  // 清空提示
  errorMsg.value = ''
  loading.value = false
}

//表单校验
const loginRules = {
  account: [
    { required: true, message: '请输入账号' }
  ],
  password: [
    { required: true, message: '请输入密码' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' }
  ],
  confirmPwd: [
    { required: true, message: '请确认密码' },
    { same: 'password', message: '两次密码不一致' }
  ]
}

const validate = (formType) => {
  return new Promise((resolve) => {
    errorMsg.value = ''
    let data = {}
    let rules = {}

    if (formType === 'login') {
      data = { account: account.value, password: password.value }
      rules = loginRules
    } else {
      data = {
        username: username.value,
        phone: phone.value,
        email: regEmail.value,
        password: regPassword.value,
        confirmPwd: regConfirmPwd.value
      }
      rules = registerRules
    }

    // 开始校验
    for (let key in rules) {
      const val = data[key]
      const ruleList = rules[key]

      for (let rule of ruleList) {
        // 必填
        if (rule.required && !val) {
          errorMsg.value = rule.message
          return resolve(false)
        }
        // 最小长度
        if (rule.min && val.length < rule.min) {
          errorMsg.value = rule.message
          return resolve(false)
        }
        //邮箱具体规则
        if (key === 'email') {
          const e = val.trim()
          const atPos = e.indexOf('@')

          if (atPos === -1) {
            errorMsg.value = '邮箱必须包含 @ 符号'
            return resolve(false)
          }
          if (atPos === 0) {
            errorMsg.value = '邮箱格式错误，@ 前面不能没有内容'
            return resolve(false)
          }
          const domain = e.slice(atPos + 1)
          if (!domain) {
            errorMsg.value = '邮箱格式错误，@ 后面需要填写域名（如 qq.com）'
            return resolve(false)
          }
          const dotPos = domain.lastIndexOf('.')
          if (dotPos === -1) {
            errorMsg.value = '邮箱域名缺少后缀（如 .com/.cn/.net）'
            return resolve(false)
          }
          if (dotPos >= domain.length - 2) {
            errorMsg.value = '邮箱后缀过短,至少2位'
            return resolve(false)
          }
        }
        // 正则
        if (rule.pattern && !rule.pattern.test(val)) {
          errorMsg.value = rule.message
          return resolve(false)
        }
        // 确认密码
        if (rule.same) {
          if (val !== data[rule.same]) {
            errorMsg.value = rule.message
            return resolve(false)
          }
        }
      }
    }

    resolve(true)
  })
}
</script>


<template>
  <div class="page">

    <div class="left"
      :style="{ background: `linear-gradient(135deg, ${primaryColor}e6, ${primaryColor}, ${primaryColor}cc)` }">
      <div class="brand">
        <div class="brand-icon">
          <Sparkles :size="16" />
        </div>
        <span>{{ brandName }}</span>
      </div>
      <div class="characters-area">
        <AnimatedCharacters :is-typing="isTyping" :has-secret="!!currentPassword" :secret-visible="showPassword" />
      </div>
      <div class="footer-links">
        <a href="#">隐私政策</a>
        <a href="#">服务条款</a>
        <a href="#">联系</a>
      </div>
      <div class="deco-grid" />
      <div class="deco-circle deco-circle-1" />
      <div class="deco-circle deco-circle-2" />
    </div>

    <div class="right">
      <div class="form-wrapper">
        <div class="mobile-brand">
          <div class="brand-icon">
            <Sparkles :size="16" />
          </div>
          <span>{{ brandName }}</span>
        </div>

        <!-- 登录 -->
        <div v-if="isLogin">
          <div class="header">
            <h1>{{ title }}</h1>
            <p>{{ subtitle }}</p>
          </div>
          <form @submit.prevent="onSubmit" class="form">
            <div class="field">
              <label for="login-account">账号</label>
              <input id="login-account" type="text" :placeholder="accountPlaceholder" v-model="account"
                autocomplete="off" @focus="isTyping = true" @blur="isTyping = false" />
            </div>
            <div class="field">
              <label for="login-password">密码</label>
              <div class="password-wrap">
                <input id="login-password" :type="showLoginPwd ? 'text' : 'password'" placeholder="请输入密码"
                  v-model="password" />
                <button type="button" class="eye-btn" @click="showLoginPwd = !showLoginPwd">
                  <EyeOff v-if="showLoginPwd" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <Captcha ref="captchaComponent" @captchaSuccess="handleCaptchaSuccess" @captchaFail="handleCaptchaFail" />
            <div class="field" v-if="needLoginCaptcha">
              <label>验证码</label>
              <div class="captcha-wrap">
                <input v-model="loginEmailCode" placeholder="请输入邮箱验证码" />
                <button type="button" class="captcha-btn" :disabled="loginCodeBtnDisabled" @click="sendLoginEmailCode">
                  {{ loginCodeBtnText }}
                </button>
              </div>
            </div>
            <div class="options">
              <label class="remember"><input type="checkbox" v-model="remember" /> 记住密码</label>
              <a href="#" class="forgot">找回密码？</a>
            </div>
            <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
            <button type="submit" class="btn-primary" :disabled="loading" :style="{ background: primaryColor }">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
          <!-- <button v-if="showGoogleLogin" type="button" class="btn-google">
            <Mail :size="20" /> 使用Google登录
          </button> -->
          <p class="signup-link">没有账号怎么办? <a @click="isLogin = false; resetForm()">去注册</a></p>
        </div>

        <!-- 注册 -->
        <div v-else>
          <div class="header">
            <h1>创建您的账号</h1>
            <p>填写信息完成注册</p>
          </div>
          <form @submit.prevent="onRegister" class="form" novalidate>
            <div class="field">
              <label>昵称</label>
              <input type="text" placeholder="请输入昵称(可选填)" v-model="nickname" />
            </div>
            <div class="field">
              <label>账号</label>
              <input type="text" placeholder="请输入用户名" v-model="username" />
            </div>
            <div class="field">
              <label>手机号</label>
              <input type="text" placeholder="请输入手机号" v-model="phone" />
            </div>
            <div class="field">
              <label>邮箱</label>
              <input type="text" placeholder="请输入邮箱" v-model="regEmail" />
            </div>

            <div class="field">
              <label>设置密码</label>
              <div class="password-wrap">
                <input :type="showRegPwd ? 'text' : 'password'" v-model="regPassword" placeholder="请输入密码" />
                <button type="button" class="eye-btn" @click="showRegPwd = !showRegPwd">
                  <EyeOff v-if="showRegPwd" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <div class="field">
              <label>确认密码</label>
              <div class="password-wrap">
                <input :type="showRegPwd ? 'text' : 'password'" v-model="regConfirmPwd" placeholder="请输入确认密码" />
                <button type="button" class="eye-btn" @click="showRegPwd = !showRegPwd">
                  <EyeOff v-if="showRegPwd" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <div class="field" v-if="needEmailCode">
              <label>验证码</label>
              <div class="captcha-wrap">
                <input v-model="emailCode" placeholder="请输入验证码" />
                <button type="button" class="captcha-btn" :disabled="codeBtnDisabled" @click="sendEmailCode">
                  {{ codeBtnText }}
                </button>
              </div>
            </div>

            <!-- <div class="options">
              <label class="remember"><input type="checkbox" v-model="regAgree" /> 我已阅读并同意</label>
            </div> -->

            <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

            <button type="submit" class="btn-primary" :disabled="loading" :style="{ background: primaryColor }">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>

          <p class="signup-link">已有账号？<a @click="isLogin = true; resetForm()">去登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
.page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 80vh;
}

.left {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  color: white;
  overflow: hidden;
}

.brand {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.characters-area {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 500px;
}

.footer-links {
  position: relative;
  z-index: 20;
  display: flex;
  gap: 32px;
  font-size: 14px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
}

.deco-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(48px);
}

.deco-circle-1 {
  top: 25%;
  right: 25%;
  width: 256px;
  height: 256px;
  background: rgba(255, 255, 255, 0.1);
}

.deco-circle-2 {
  bottom: 25%;
  left: 25%;
  width: 384px;
  height: 384px;
  background: rgba(255, 255, 255, 0.05);
}

.right {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #fff;
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
}

.mobile-brand {
  display: none;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.5px;
  margin-bottom: 8px;
}

.header p {
  color: #71717a;
  font-size: 14px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.field label {
  width: 60px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 550;
  text-align: justify;
  text-align-last: justify;
}

.field input {
  height: 48px;
  width: 600px;
  padding: 0 12px;
  border-radius: 6px;
  border: 1px solid #d4d4d8;
  font-size: 15px;
  outline: none;
  background: #fff;
  transition: border-color 0.2s;
  flex: 1;
  min-width: 0;
}

.field input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.2);
}

/** 验证码样式 */
.captcha-wrap {
  flex: 0 0 60%;
  position: relative;
  display: flex;
  align-items: center;
}

.captcha-wrap input {
  height: 48px;
  width: 100%;
  padding: 0 100px 0 12px;
  border-radius: 6px;
  border: 1px solid #d4d4d8;
  font-size: 15px;
  outline: none;
  background: #fff;
  box-sizing: border-box;
}

.captcha-wrap input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.2);
}

.captcha-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  padding: 0 12px;
  height: 34px;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.captcha-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.password-wrap {
  flex: 1;
  position: relative;
}

.password-wrap input {
  width: 100%;
  padding-right: 40px;
}

.eye-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a1a1aa;
  transition: color 0.2s;
}

.eye-btn:hover {
  color: #3f3f46;
}

.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.forgot {
  text-decoration: none;
  color: #4f46e5;
  font-weight: 500;
}

.forgot:hover {
  color: #e5464e;
  text-decoration: none;
}

.error-msg {
  padding: 12px;
  font-size: 14px;
  color: #f87171;
  background: rgba(127, 29, 29, 0.08);
  border: 1px solid rgba(127, 29, 29, 0.15);
  border-radius: 8px;
}

.btn-primary {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  color: white;
  transition: opacity 0.2s;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-google {
  width: 100%;
  height: 48px;
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 6px;
  border: 1px solid #d4d4d8;
  background: #fff;
  font-size: 14px;
  transition: background 0.2s;
}

.btn-google:hover {
  background: #c2c1c1;
}

.signup-link {
  text-decoration: none;
  text-align: center;
  font-size: 14px;
  color: #71717a;
  margin-top: 32px;
}

.signup-link a {
  cursor: pointer;
  text-decoration: none;
  color: #4f46e5;
  font-weight: 500;
}

.signup-link a:hover {
  color: #e54646;
  text-decoration: none;
}

@media (max-width: 1023px) {
  .page {
    grid-template-columns: 1fr;
  }

  .left {
    display: none;
  }

  .mobile-brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 48px;
  }
}

/* 平板响应式 */
@media (max-width: 768px) {
  .right {
    padding: 24px 20px;
  }

  .form-wrapper {
    max-width: 100%;
  }

  .field {
    gap: 8px;
  }

  .field label {
    width: 60px;
    /* 小屏稍微减小标签宽度 */
    font-size: 13px;
  }

  .field input {
    height: 48px;
    font-size: 14px;
  }
}

/* 手机响应式 - 改回上下布局 */
@media (max-width: 640px) {
  .right {
    padding: 60px 24px 40px;
    align-items: flex-start;
  }

  .form-wrapper {
    width: 100%;
    margin-top: 20px;
  }

  .field {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
  }

  .field label {
    width: auto;
    text-align: left;
    text-align-last: left;
    font-size: 14px;
    font-weight: 550;
  }

  /* 确保所有输入框和密码框都占满宽度 */
  .field input,
  .password-wrap {
    width: 100%;
    flex: none;
  }

  .field input {
    width: 100%;
    box-sizing: border-box;
  }

  /**验证码样式 */
  .captcha-wrap {
    flex: none;
    width: 100%;
  }

  .captcha-wrap input {
    width: 100%;
  }

  .password-wrap {
    width: 100%;
  }

  .password-wrap input {
    width: 100%;
    box-sizing: border-box;
  }

  .header h1 {
    font-size: 24px;
  }

  .header p {
    font-size: 13px;
  }

  .btn-primary {
    height: 44px;
    font-size: 15px;
  }
}
</style>
