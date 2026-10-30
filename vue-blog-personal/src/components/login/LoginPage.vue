<!--
登录 UI 组件来源开源项目
原仓库地址：https://gitee.com/niumg9527/login-animation
原作者：niumg9527
开源协议：MIT License
Copyright (c) 2026 niumg9527
修改说明：仅新增注册功能、适配个人博客登录业务逻辑，完整保留原作布局、样式、动画交互逻辑。
使用范围：个人非商用博客，严格遵循原开源协议全部条款。
-->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Eye, EyeOff, Sparkles } from 'lucide-vue-next';
import AnimatedCharacters from './AnimatedCharacters.vue';
import Captcha from '@/components/Captcha.vue';
import { getOrCreateDeviceId, getRememberedAccount } from '@/utils/auth';
import { sendLoginCodeApi, sendRegisterCodeApi } from '@/api/auth';

const props = defineProps({
  brandName: {
    type: String,
    default: '您好',
  },
  title: {
    type: String,
    default: '您好，欢迎你!',
  },
  subtitle: {
    type: String,
    default: '请输入您的信息',
  },
  accountPlaceholder: {
    type: String,
    default: '请输入用户名/手机号/邮箱',
  },
  primaryColor: {
    type: String,
    default: '#4f46e5',
  },
  showGoogleLogin: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['login-submit', 'login-verify-submit', 'register-submit']);

const isLogin = ref(true);

// 登录表单：回填上次勾选"记住我"时保存的账号，密码不回填
const rememberedAccount = getRememberedAccount();
const account = ref(rememberedAccount);
const password = ref('');
const remember = ref(!!rememberedAccount);

// 注册表单
const nickname = ref('');
const username = ref('');
const phone = ref('');
const regEmail = ref('');
const regPassword = ref('');
const regConfirmPwd = ref('');
const regAgree = ref(false);

// 登录验证码
const needLoginEmailCode = ref(false);
const loginEmailCode = ref('');
const loginCodeBtnDisabled = ref(false);
const loginCodeBtnText = ref('获取验证码');
const loginRiskTicket = ref('');
const loginMaskedEmail = ref('');
let loginCodeTimer = null;
// 注册验证码
const codeBtnDisabled = ref(false);
const codeBtnText = ref('获取验证码');
const emailCode = ref('');
let registerCodeTimer = null;

// 展示密码动画状态
const showLoginPwd = ref(false);
const showRegPwd = ref(false);

// 状态
const errorMsg = ref('');
const loading = ref(false);
const isTyping = ref(false);

/** 为父级暴露以控制错误/加载状态 */
defineExpose({
  setError: (msg) => {
    errorMsg.value = msg;
  },
  setLoading: (v) => {
    loading.value = v;
  },
  registrationSucceeded: () => {
    isLogin.value = true;
    errorMsg.value = '';
  },
  requireLoginEmailCode: (riskInfo) => {
    needLoginEmailCode.value = true;
    loginRiskTicket.value = riskInfo?.riskTicket || '';
    loginMaskedEmail.value = riskInfo?.maskedEmail || '';
    loginEmailCode.value = '';
    loading.value = false;
    errorMsg.value = `当前为${riskInfo?.riskReason || '风险登录'}，请完成邮箱验证`;
  },
});

const currentPassword = computed(() => {
  return isLogin.value ? password.value : regPassword.value;
});
const showPassword = computed(() => {
  return isLogin.value ? showLoginPwd.value : showRegPwd.value;
});

// 滑块验证：通过后才允许把账号密码提交给后端
const captchaComponent = ref(null);
let pendingLoginData = null;

const handleCaptchaFail = () => {
  // 阿里云 SDK 可能在成功回调之后继续触发 onClose，此时不能覆盖登录状态
  if (!pendingLoginData) return;
  pendingLoginData = null;
  loading.value = false;
  errorMsg.value = '安全验证已取消，请重新登录';
};

const handleCaptchaSuccess = (captchaVerifyParam) => {
  if (!pendingLoginData) {
    loading.value = false;
    return;
  }
  if (!captchaVerifyParam) {
    handleCaptchaFail();
    return;
  }

  emit('login-submit', {
    ...pendingLoginData,
    captchaVerifyParam,
  });
  pendingLoginData = null;
};

// 登录
const onSubmit = async () => {
  const valid = await validate(needLoginEmailCode.value ? 'loginVerify' : 'login');
  if (!valid) return;

  loading.value = true;
  errorMsg.value = '';

  if (needLoginEmailCode.value) {
    emit('login-verify-submit', {
      riskTicket: loginRiskTicket.value,
      emailCode: loginEmailCode.value,
      deviceId: getOrCreateDeviceId(),
      remember: remember.value,
    });
    return;
  }

  pendingLoginData = {
    account: account.value,
    password: password.value,
    remember: remember.value,
    deviceId: getOrCreateDeviceId(),
  };

  if (captchaComponent.value) {
    captchaComponent.value.show();
  } else {
    handleCaptchaFail();
    errorMsg.value = '安全验证组件未加载，请刷新页面重试';
  }
};

// 登录发送邮箱验证码
const sendLoginEmailCode = async () => {
  if (!loginRiskTicket.value) {
    errorMsg.value = '登录验证凭证已失效，请重新登录';
    return;
  }
  if (loginCodeTimer) return;

  loginCodeBtnDisabled.value = true;
  errorMsg.value = '';
  try {
    const result = await sendLoginCodeApi({ riskTicket: loginRiskTicket.value });
    if (result.code !== 200) {
      errorMsg.value = result.msg || '验证码发送失败';
      loginCodeBtnDisabled.value = false;
      return;
    }

    let seconds = 60;
    loginCodeBtnText.value = `重新发送(${seconds}s)`;
    loginCodeTimer = setInterval(() => {
      seconds -= 1;
      if (seconds <= 0) {
        clearInterval(loginCodeTimer);
        loginCodeTimer = null;
        loginCodeBtnDisabled.value = false;
        loginCodeBtnText.value = '获取验证码';
      } else {
        loginCodeBtnText.value = `重新发送(${seconds}s)`;
      }
    }, 1000);
  } catch (error) {
    errorMsg.value = error?.response?.data?.msg || '验证码发送失败，请稍后重试';
    loginCodeBtnDisabled.value = false;
  }
};

// 注册提交
const onRegister = async () => {
  const valid = await validate('register');
  if (!valid) return;

  // captcha.show();
  loading.value = true;
  errorMsg.value = '';

  emit('register-submit', {
    nickname: nickname.value,
    username: username.value,
    phone: phone.value,
    email: regEmail.value,
    password: regPassword.value,
    confirmPwd: regConfirmPwd.value,
    emailCode: emailCode.value,
    deviceId: getOrCreateDeviceId(),
  });
};

// 发送邮箱验证码
const sendEmailCode = async () => {
  const email = regEmail.value.trim();
  if (!email) {
    errorMsg.value = '请先填写邮箱';
    return;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errorMsg.value = '邮箱格式错误';
    return;
  }
  if (registerCodeTimer) {
    return;
  }

  errorMsg.value = '';
  codeBtnDisabled.value = true;
  try {
    const result = await sendRegisterCodeApi({ email });
    if (result.code !== 200) {
      errorMsg.value = result.msg || '验证码发送失败';
      codeBtnDisabled.value = false;
      return;
    }

    let seconds = 60;
    codeBtnText.value = `重新发送(${seconds}s)`;
    registerCodeTimer = setInterval(() => {
      seconds -= 1;
      if (seconds <= 0) {
        clearInterval(registerCodeTimer);
        registerCodeTimer = null;
        codeBtnDisabled.value = false;
        codeBtnText.value = '获取验证码';
      } else {
        codeBtnText.value = `重新发送(${seconds}s)`;
      }
    }, 1000);
  } catch (error) {
    errorMsg.value = error?.response?.data?.msg || '验证码发送失败，请稍后重试';
    codeBtnDisabled.value = false;
  }
};

// 清除风险登录状态
const resetLoginVerification = () => {
  pendingLoginData = null;
  if (loginCodeTimer) {
    clearInterval(loginCodeTimer);
    loginCodeTimer = null;
  }
  needLoginEmailCode.value = false;
  loginRiskTicket.value = '';
  loginMaskedEmail.value = '';
  loginEmailCode.value = '';
  loginCodeBtnDisabled.value = false;
  loginCodeBtnText.value = '获取验证码';
  errorMsg.value = '';
  loading.value = false;
};

// 重置表单（切换登录/注册时调用）
const resetForm = () => {
  if (registerCodeTimer) {
    clearInterval(registerCodeTimer);
    registerCodeTimer = null;
  }
  resetLoginVerification();
  // 清空输入
  // 账号恢复成"记住我"保存的值，切换登录/注册回来时不用重新输入
  account.value = rememberedAccount;
  password.value = '';
  remember.value = !!rememberedAccount;

  nickname.value = '';
  username.value = '';
  phone.value = '';
  regEmail.value = '';
  regPassword.value = '';
  regConfirmPwd.value = '';
  emailCode.value = '';
  // 清空验证状态
  codeBtnDisabled.value = false;
  codeBtnText.value = '获取验证码';

  // 清空提示
  errorMsg.value = '';
  loading.value = false;
};

onUnmounted(() => {
  if (loginCodeTimer) {
    clearInterval(loginCodeTimer);
    loginCodeTimer = null;
  }
  if (registerCodeTimer) {
    clearInterval(registerCodeTimer);
    registerCodeTimer = null;
  }
});

//表单校验
const loginRules = {
  account: [{ required: true, message: '请输入账号' }],
  password: [{ required: true, message: '请输入密码' }],
};

const loginVerifyRules = {
  emailCode: [
    { required: true, message: '请输入邮箱验证码' },
    { pattern: /^\d{6}$/, message: '邮箱验证码应为6位数字' },
  ],
};

const registerRules = {
  username: [{ pattern: /^[a-zA-Z0-9_]{3,16}$/, message: '用户名需为3-16位字母、数字或下划线' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }],
  email: [{ required: true, message: '请输入邮箱' }],
  emailCode: [
    { required: true, message: '请输入验证码' },
    { pattern: /^\d{6}$/, message: '验证码应为6位数字' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码至少6位' },
  ],
  confirmPwd: [
    { required: true, message: '请确认密码' },
    { same: 'password', message: '两次密码不一致' },
  ],
};

const validate = (formType) => {
  return new Promise((resolve) => {
    errorMsg.value = '';
    let data = {};
    let rules = {};

    if (formType === 'login') {
      data = { account: account.value, password: password.value };
      rules = loginRules;
    } else if (formType === 'loginVerify') {
      data = { emailCode: loginEmailCode.value };
      rules = loginVerifyRules;
    } else {
      data = {
        username: username.value,
        phone: phone.value,
        email: regEmail.value,
        emailCode: emailCode.value,
        password: regPassword.value,
        confirmPwd: regConfirmPwd.value,
      };
      rules = registerRules;
    }

    // 开始校验
    for (let key in rules) {
      const val = data[key];
      const ruleList = rules[key];

      for (let rule of ruleList) {
        // 非必填字段为空时跳过格式校验
        if (!val && !rule.required) {
          continue;
        }
        // 必填
        if (rule.required && !val) {
          errorMsg.value = rule.message;
          return resolve(false);
        }
        // 最小长度
        if (rule.min && val.length < rule.min) {
          errorMsg.value = rule.message;
          return resolve(false);
        }
        //邮箱具体规则
        if (key === 'email') {
          const e = val.trim();
          const atPos = e.indexOf('@');

          if (atPos === -1) {
            errorMsg.value = '邮箱必须包含 @ 符号';
            return resolve(false);
          }
          if (atPos === 0) {
            errorMsg.value = '邮箱格式错误，@ 前面不能没有内容';
            return resolve(false);
          }
          const domain = e.slice(atPos + 1);
          if (!domain) {
            errorMsg.value = '邮箱格式错误，@ 后面需要填写域名（如 qq.com）';
            return resolve(false);
          }
          const dotPos = domain.lastIndexOf('.');
          if (dotPos === -1) {
            errorMsg.value = '邮箱域名缺少后缀（如 .com/.cn/.net）';
            return resolve(false);
          }
          if (dotPos >= domain.length - 2) {
            errorMsg.value = '邮箱后缀过短,至少2位';
            return resolve(false);
          }
        }
        // 正则
        if (rule.pattern && !rule.pattern.test(val)) {
          errorMsg.value = rule.message;
          return resolve(false);
        }
        // 确认密码
        if (rule.same) {
          if (val !== data[rule.same]) {
            errorMsg.value = rule.message;
            return resolve(false);
          }
        }
      }
    }

    resolve(true);
  });
};
</script>

<template>
  <div class="page">
    <div
      class="left"
      :style="{
        background: `linear-gradient(135deg, ${primaryColor}e6, ${primaryColor}, ${primaryColor}cc)`,
      }"
    >
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
        <!-- <a href="#">隐私政策</a>
        <a href="#">服务条款</a>
        <a href="#">联系</a> -->
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
              <input
                id="login-account"
                type="text"
                :placeholder="accountPlaceholder"
                v-model="account"
                :disabled="needLoginEmailCode"
                autocomplete="off"
                @focus="isTyping = true"
                @blur="isTyping = false"
              />
            </div>
            <div class="field">
              <label for="login-password">密码</label>
              <div class="password-wrap">
                <input
                  id="login-password"
                  :type="showLoginPwd ? 'text' : 'password'"
                  placeholder="请输入密码"
                  v-model="password"
                  :disabled="needLoginEmailCode"
                />
                <button type="button" class="eye-btn" @click="showLoginPwd = !showLoginPwd">
                  <EyeOff v-if="showLoginPwd" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <Captcha ref="captchaComponent" @captchaSuccess="handleCaptchaSuccess" @captchaFail="handleCaptchaFail" />
            <div class="field login-code-field" v-if="needLoginEmailCode">
              <label for="login-email-code">验证码</label>
              <div class="login-code-control">
                <div class="captcha-wrap">
                  <input
                    id="login-email-code"
                    v-model="loginEmailCode"
                    placeholder="请输入6位验证码"
                    maxlength="6"
                    inputmode="numeric"
                  />
                  <button type="button" class="captcha-btn" :disabled="loginCodeBtnDisabled" @click="sendLoginEmailCode">
                    {{ loginCodeBtnText }}
                  </button>
                </div>
                <p class="login-code-hint">验证码将发送至 {{ loginMaskedEmail }}</p>
              </div>
            </div>
            <div class="options">
              <label class="remember"
                ><input type="checkbox" v-model="remember" :disabled="needLoginEmailCode" /> 记住我</label
              >
              <a v-if="!needLoginEmailCode" href="/forgotpwd" class="forgot">忘记密码？</a>
              <a v-else href="#" class="forgot" @click.prevent="resetLoginVerification">更换账号</a>
            </div>
            <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
            <button type="submit" class="btn-primary" :disabled="loading" :style="{ background: primaryColor }">
              {{
                loading ? (needLoginEmailCode ? '验证中...' : '登录中...') : needLoginEmailCode ? '验证并登录' : '登录'
              }}
            </button>
          </form>
          <!-- <button v-if="showGoogleLogin" type="button" class="btn-google">
            <Mail :size="20" /> 使用Google登录
          </button> -->
          <p class="signup-link">
            没有账号怎么办?
            <a
              @click="
                isLogin = false;
                resetForm();
              "
              >去注册</a
            >
          </p>
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
              <input type="text" placeholder="请输入昵称" v-model="nickname" />
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
              <input type="text" placeholder="请输入邮箱 *" v-model="regEmail" />
            </div>

            <div class="field">
              <label>验证码</label>
              <div class="captcha-wrap">
                <input v-model="emailCode" placeholder="请输入验证码" maxlength="6" inputmode="numeric" />
                <button type="button" class="captcha-btn" :disabled="codeBtnDisabled" @click="sendEmailCode">
                  {{ codeBtnText }}
                </button>
              </div>
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

            <!-- <div class="options">
              <label class="remember"><input type="checkbox" v-model="regAgree" /> 我已阅读并同意</label>
            </div> -->

            <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

            <button type="submit" class="btn-primary" :disabled="loading" :style="{ background: primaryColor }">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>

          <p class="signup-link">
            已有账号？<a
              @click="
                isLogin = true;
                resetForm();
              "
              >去登录</a
            >
          </p>
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
.login-code-control {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.login-code-hint {
  margin: 0;
  color: #71717a;
  font-size: 12px;
  line-height: 1.45;
  text-align: left;
  overflow-wrap: anywhere;
}

.captcha-wrap {
  flex: 1;
  width: 100%;
  min-width: 0;
  position: relative;
  display: flex;
  align-items: center;
}

.captcha-wrap input {
  height: 48px;
  width: 100%;
  padding: 0 126px 0 12px;
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
  max-width: 112px;
  padding: 0 12px;
  height: 34px;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  /* 加内边距扩大点击区域，同时把 right 相应减小，图标视觉位置不变 */
  right: 4px;
  padding: 8px;
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
  .login-code-control {
    flex: none;
    width: 100%;
  }

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
