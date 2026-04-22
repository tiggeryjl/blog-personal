<script setup>
import { ref, computed, onMounted } from 'vue'
import { Eye, EyeOff, Mail, Sparkles } from 'lucide-vue-next'
import AnimatedCharacters from './AnimatedCharacters.vue'

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
  emailPlaceholder: {
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

// const emit = defineEmits(['submit'])
const emit = defineEmits(['login-submit', 'register-submit'])

const isLogin = ref(true)

// 登录表单
const email = ref('')
const password = ref('')
const remember = ref(false)

// 注册表单
const regPassword = ref('')
const regConfirmPwd = ref('')
const regAgree = ref(false)

//验证码
const needEmailCode = ref(false)
const codeBtnDisabled = ref(false)
const codeBtnText = ref('获取验证码')
let captcha = null

// 展示密码动画状态
const showPassword = ref(false)

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

// 登录提交
const onSubmit = () => {
  errorMsg.value = ''
  emit('login-submit', {
    email: email.value,
    password: password.value,
    remember: remember.value,
  })
}

// 注册提交
const onRegister = () => {
  // captcha.show();
  errorMsg.value = '';
  if (regPassword.value !== regConfirmPwd.value) {
    errorMsg.value = '两次密码不一致';
    return;
  }
  emit('register-submit', {
    email: email.value,
    password: regPassword.value
  })
}

/*onMounted(() => {
  captcha = new window.TencentCaptcha('你的APPID', captchaBack)
})*/

const captchaBack = async (res) => {
  if (res.ret !== 0) return alert('滑块失败')

  localStorage.setItem('lastTicket', res.ticket)
  localStorage.setItem('lastRandstr', res.randstr)

  // 先检查是否需要邮箱验证
  const { data } = await axios.post('/api/register/check', {
    username: username.value,
    password: password.value,
    email: email.value,
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
  await axios.post('/api/register/send-email-code', { email: email.value })
  // 倒计时逻辑自己加
}
// 确认注册（带邮箱验证码）
const confirmRegister = async () => {
  await axios.post('/api/register/confirm', {
    username: username.value,
    password: password.value,
    email: email.value,
    emailCode: emailCode.value,
    ticket: localStorage.getItem('lastTicket'),
    randstr: localStorage.getItem('lastRandstr')
  })
  alert('注册成功')
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
              <label for="login-email">账号</label>
              <input id="login-email" type="email" :placeholder="emailPlaceholder" v-model="email" autocomplete="off"
                @focus="isTyping = true" @blur="isTyping = false" required />
            </div>
            <div class="field">
              <label for="login-password">密码</label>
              <div class="password-wrap">
                <input id="login-password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码"
                  v-model="password" required />
                <button type="button" class="eye-btn" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="20" />
                  <Eye v-else :size="20" />
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
          <p class="signup-link">没有账号怎么办? <a @click="isLogin = false">去注册</a></p>
        </div>

        <!-- 注册 -->
        <div v-else>
          <div class="header">
            <h1>创建您的账号</h1>
            <p>填写信息完成注册</p>
          </div>
          <form @submit.prevent="onRegister" class="form">
            <div class="field">
              <label>昵称</label>
              <input id="register-email" type="email" placeholder="请输入昵称(可选填)" v-model="email" autocomplete="off"
                @focus="isTyping = true" @blur="isTyping = false" required />
            </div>
            <div class="field">
              <label>账号</label>
              <input id="register-email" type="email" placeholder="请输入用户名" v-model="email" autocomplete="off"
                @focus="isTyping = true" @blur="isTyping = false" required />
            </div>
            <div class="field">
              <label>手机号</label>
              <input id="register-email" type="email" placeholder="请输入手机号" v-model="email" autocomplete="off"
                @focus="isTyping = true" @blur="isTyping = false" required />
            </div>
            <div class="field">
              <label>邮箱</label>
              <input id="register-email" type="email" placeholder="请输入邮箱" v-model="email" autocomplete="off"
                @focus="isTyping = true" @blur="isTyping = false" required />
            </div>

            <div class="field">
              <label>设置密码</label>
              <div class="password-wrap">
                <input :type="showPassword ? 'text' : 'password'" v-model="regPassword" placeholder="请输入密码" required />
                <button type="button" class="eye-btn" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <div class="field">
              <label>确认密码</label>
              <div class="password-wrap">
                <input :type="showPassword ? 'text' : 'password'" v-model="regConfirmPwd" placeholder="请输入确认密码"
                  required />
                <button type="button" class="eye-btn" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="20" />
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

          <p class="signup-link">已有账号？<a @click="isLogin = true">去登录</a></p>
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
