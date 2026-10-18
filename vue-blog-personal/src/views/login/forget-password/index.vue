<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { sendResetPwdCodeApi, verifyResetPwdCodeApi, resetPwdByTicketApi } from '@/api/auth';
import { getRememberedAccount } from '@/utils/auth';

const router = useRouter();

const currentStep = ref(1);
const ticket = ref('');

// 上次勾选"记住我"保存的账号如果是邮箱，直接带进来省得再输一遍
const rememberedAccount = getRememberedAccount();

const form = reactive({
  email: rememberedAccount.includes('@') ? rememberedAccount : '',
  emailCode: '',
  newPassword: '',
  confirmPwd: '',
});

const sending = ref(false);
const verifying = ref(false);
const submitting = ref(false);
const countdown = ref(0);
let countdownTimer = null;

const canSendCode = computed(() => !!form.email.trim() && countdown.value === 0 && !sending.value);

// 发送成功后 60 秒倒计时，防止被刷
const startCountdown = () => {
  countdown.value = 60;
  countdownTimer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

const sendCode = async () => {
  const email = form.email.trim();
  if (!email) {
    ElMessage.warning('请先填写注册邮箱');
    return;
  }
  if (!canSendCode.value) return;

  sending.value = true;
  try {
    const result = await sendResetPwdCodeApi({ email });
    if (result.code === 200) {
      ElMessage.success('验证码已发送，请查收邮件');
      startCountdown();
    } else {
      ElMessage.error(result.msg || '验证码发送失败，请稍后重试');
    }
  } catch (err) {
    ElMessage.error('验证码发送失败，请稍后重试');
  } finally {
    sending.value = false;
  }
};

// 第一步：校验验证码，通过后拿到一次性凭证
const goStepTwo = async () => {
  const email = form.email.trim();
  const emailCode = form.emailCode.trim();

  if (!email) {
    ElMessage.warning('请填写注册邮箱');
    return;
  }
  if (!emailCode) {
    ElMessage.warning('请填写邮箱验证码');
    return;
  }

  verifying.value = true;
  try {
    const result = await verifyResetPwdCodeApi({ email, emailCode });
    if (result.code === 200 && result.data) {
      ticket.value = result.data;
      currentStep.value = 2;
    } else {
      ElMessage.error(result.msg || '验证失败，请检查验证码');
    }
  } catch (err) {
    ElMessage.error('验证失败，请稍后重试');
  } finally {
    verifying.value = false;
  }
};

// 第二步：设置新密码
const submitPassword = async () => {
  const newPassword = form.newPassword.trim();
  const confirmPwd = form.confirmPwd.trim();

  if (newPassword.length < 6 || newPassword.length > 20) {
    ElMessage.warning('密码长度需为 6-20 位');
    return;
  }
  if (newPassword !== confirmPwd) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }

  submitting.value = true;
  try {
    const result = await resetPwdByTicketApi({ ticket: ticket.value, newPassword, confirmPwd });
    if (result.code === 200) {
      currentStep.value = 3;
    } else {
      ElMessage.error(result.msg || '重置失败，请稍后重试');
    }
  } catch (err) {
    ElMessage.error('重置失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 返回第一步重新验证，密码清空
const backToStepOne = () => {
  form.newPassword = '';
  form.confirmPwd = '';
  currentStep.value = 1;
};

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
});
</script>

<template>
  <div class="forget-page">
    <span class="glow glow-left"></span>
    <span class="glow glow-right"></span>

    <div class="forget-inner">
      <h1 class="page-title">忘记密码</h1>

      <div class="step-bar">
        <div class="step-item" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
          <span class="step-index">{{ currentStep > 1 ? '✓' : '1' }}</span>
          <span class="step-text">验证邮箱</span>
        </div>
        <div class="step-item" :class="{ active: currentStep >= 2, done: currentStep > 2 }">
          <span class="step-index">{{ currentStep > 2 ? '✓' : '2' }}</span>
          <span class="step-text">设置新密码</span>
        </div>
        <div class="step-item" :class="{ active: currentStep >= 3 }">
          <span class="step-index">{{ currentStep >= 3 ? '✓' : '3' }}</span>
          <span class="step-text">完成</span>
        </div>
      </div>

      <transition name="step-fade" mode="out-in">
        <!-- 第一步：验证邮箱 -->
        <section v-if="currentStep === 1" key="step1" class="step-body">
          <h2 class="step-title">验证邮箱</h2>
          <p class="step-tip">填写注册邮箱，我们会发送一封带验证码的邮件</p>

          <div class="form-item">
            <label>注册邮箱</label>
            <input v-model="form.email" type="email" placeholder="请输入注册时使用的邮箱" @keyup.enter="sendCode" />
          </div>

          <div class="form-item">
            <label>邮箱验证码</label>
            <div class="code-row">
              <input
                v-model="form.emailCode"
                type="text"
                maxlength="6"
                placeholder="请输入 6 位验证码"
                @keyup.enter="goStepTwo"
              />
              <button class="code-btn" :disabled="!canSendCode" @click="sendCode">
                {{ countdown > 0 ? `${countdown} 秒后重发` : '获取验证码' }}
              </button>
            </div>
          </div>

          <button class="submit-btn" :disabled="verifying" @click="goStepTwo">
            {{ verifying ? '验证中...' : '下一步' }}
          </button>

          <p class="back-login" @click="router.replace('/login')">返回登录</p>
        </section>

        <!-- 第二步：设置新密码 -->
        <section v-else-if="currentStep === 2" key="step2" class="step-body">
          <h2 class="step-title">设置新密码</h2>
          <p class="step-tip">正在为 {{ form.email }} 重置密码</p>

          <div class="form-item">
            <label>新密码</label>
            <input v-model="form.newPassword" type="password" placeholder="6-20 位" />
          </div>

          <div class="form-item">
            <label>确认新密码</label>
            <input
              v-model="form.confirmPwd"
              type="password"
              placeholder="请再次输入新密码"
              @keyup.enter="submitPassword"
            />
          </div>

          <div class="btn-row">
            <button class="ghost-btn" @click="backToStepOne">上一步</button>
            <button class="submit-btn" :disabled="submitting" @click="submitPassword">
              {{ submitting ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </section>

        <!-- 第三步：完成 -->
        <section v-else key="step3" class="step-body">
          <div class="done-icon">✓</div>
          <h2 class="step-title">密码重置成功</h2>
          <p class="step-tip">请使用新密码登录并妥善保存好密码哦！</p>
          <button class="submit-btn" @click="router.replace('/login')">返回登录</button>
        </section>
      </transition>
    </div>
  </div>
</template>

<style scoped>
.forget-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  box-sizing: border-box;
  overflow: hidden;
  /* 这一页固定浅色：背景白色，同时把主题变量钉成浅色值，
     否则暗色主题下会白底白字看不见 */
  background: #fff;
  --text-color: #1a1a1a;
  --text-main-color: #333333;
  --text-secondary-color: #666666;
  --card-bg: #f1f3f5;
  --primary-color: #4f46e5;
  --hover-color: #818cf8;
}

/* ========== 背景光晕（纯装饰，不参与布局） ========== */
.glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: var(--primary-color);
  filter: blur(130px);
  opacity: 0.16;
  pointer-events: none;
}

.glow-left {
  top: -140px;
  left: -120px;
}

.glow-right {
  right: -140px;
  bottom: -160px;
  background: var(--hover-color);
  opacity: 0.12;
}

/* ========== 内容区：占屏宽五分之三，居中 ========== */
.forget-inner {
  position: relative;
  z-index: 1;
  width: 60%;
  max-width: 900px;
  /* 外围描边 + 三层叠加阴影，和纯白背景拉开层次 */
  padding: 44px 40px;
  box-sizing: border-box;
  border: 1px solid #e6e8eb;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 2px 6px rgba(15, 30, 60, 0.04), 0 12px 28px rgba(15, 30, 60, 0.1), 0 32px 64px rgba(15, 30, 60, 0.08);
}

.page-title {
  margin: 0 0 36px;
  font-size: 34px;
  font-weight: 600;
  letter-spacing: 6px;
  color: var(--text-color);
  text-align: center;
}

/* ========== 五边形箭头步骤条 ========== */
.step-bar {
  display: flex;
  gap: 3px;
  margin-bottom: 46px;
}

.step-item {
  flex: 1;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--card-bg);
  color: var(--text-secondary-color);
  font-size: 13px;
  letter-spacing: 1px;
  /* 右侧尖角 + 左侧内凹，形成箭头形状 */
  clip-path: polygon(0 0, calc(100% - 16px) 0, 100% 50%, calc(100% - 16px) 100%, 0 100%, 16px 50%);
  transition: background 0.35s ease, color 0.35s ease, opacity 0.35s ease;
}

/* 第一个不内凹，避免左边多一个缺口 */
.step-item:first-child {
  clip-path: polygon(0 0, calc(100% - 16px) 0, 100% 50%, calc(100% - 16px) 100%, 0 100%);
}

.step-item.active {
  background: var(--primary-color);
  color: #fff;
}

/* 已完成的稍微降一点，让当前步更突出 */
.step-item.done {
  opacity: 0.72;
}

.step-index {
  font-size: 13px;
  font-weight: 600;
}

/* ========== 步骤内容 ========== */
.step-title {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 500;
  letter-spacing: 3px;
  color: var(--text-color);
  text-align: center;
}

.step-tip {
  margin: 0 0 36px;
  font-size: 14px;
  line-height: 1.8;
  letter-spacing: 0.5px;
  color: var(--text-secondary-color);
  text-align: center;
  word-break: break-all;
}

/* ========== 表单 ========== */
.form-item {
  margin-bottom: 32px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 550;
  color: #3f3f46;
}

.form-item input {
  width: 100%;
  height: 48px;
  padding: 0 12px;
  box-sizing: border-box;
  border: 1px solid #d4d4d8;
  border-radius: 6px;
  background: #fff;
  color: #18181b;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.form-item input::placeholder {
  font-size: 14px;
  color: #a1a1aa;
}

.form-item input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.2);
}

.code-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-row input {
  /* 用 auto 而不是 0：万一以后又改成竖排，flex-basis 不会把高度压掉 */
  flex: 1 1 auto;
  min-width: 0;
}

.code-btn {
  flex-shrink: 0;
  /* 定宽，倒计时文案变长时输入框不会跟着抖动 */
  width: 118px;
  height: 40px;
  padding: 0 8px;
  box-sizing: border-box;
  border: none;
  border-radius: 6px;
  background: #4f46e5;
  color: #fff;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: opacity 0.2s;
}

.code-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.code-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* ========== 按钮 ========== */
.submit-btn {
  display: block;
  width: 20%;
  min-width: 132px;
  height: 44px;
  box-sizing: border-box;
  margin: 8px auto 0;
  border: none;
  border-radius: 6px;
  background: #4f46e5;
  color: #fff;
  font-size: 15px;
  letter-spacing: 1px;
  white-space: nowrap;
  cursor: pointer;
  transition: opacity 0.2s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-row {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 第二步的两个按钮按内容宽度排，避免比例按钮被压变形 */
.btn-row .submit-btn {
  width: auto;
  min-width: 132px;
  margin: 0;
}

.ghost-btn {
  flex-shrink: 0;
  height: 44px;
  padding: 0 26px;
  box-sizing: border-box;
  border: 1px solid #d4d4d8;
  border-radius: 6px;
  background: #fff;
  color: #3f3f46;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.ghost-btn:hover {
  border-color: #4f46e5;
  color: #4f46e5;
}

/* 表单收窄到内容区的五分之三，和整宽的步骤条形成层次 */
.form-item,
.btn-row {
  width: 60%;
  margin-left: auto;
  margin-right: auto;
}

/* ========== 完成页 ========== */
.done-icon {
  width: 78px;
  height: 78px;
  margin: 4px auto 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--primary-color);
  color: #fff;
  font-size: 36px;
  box-shadow: 0 16px 36px rgba(79, 70, 229, 0.3);
  animation: done-pop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes done-pop {
  from {
    transform: scale(0.6);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.back-login {
  margin: 26px 0 0;
  font-size: 14px;
  letter-spacing: 1px;
  color: var(--text-secondary-color);
  text-align: center;
  cursor: pointer;
  transition: color 0.3s ease;
}

.back-login:hover {
  color: var(--primary-color);
}

/* ========== 步骤切换动画 ========== */
.step-fade-enter-active,
.step-fade-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.step-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.step-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .forget-inner {
    width: 72%;
    padding: 40px 34px;
  }
}

/* 平板：外框撑满可用宽度，内部表单同步拉满 */
@media (max-width: 992px) {
  .forget-inner {
    width: 100%;
    max-width: 720px;
    padding: 36px 28px;
  }

  .page-title {
    margin-bottom: 28px;
    font-size: 28px;
    letter-spacing: 4px;
  }

  .step-bar {
    margin-bottom: 34px;
  }

  .step-title {
    font-size: 20px;
  }

  .step-tip {
    margin-bottom: 28px;
  }

  .form-item,
  .btn-row {
    width: 100%;
  }
}

/* 小屏平板 / 大屏手机 */
@media (max-width: 768px) {
  .forget-page {
    padding: 24px 16px;
  }

  .forget-inner {
    padding: 28px 22px;
    border-radius: 14px;
  }

  .page-title {
    margin-bottom: 24px;
    font-size: 24px;
    letter-spacing: 3px;
  }

  .step-bar {
    gap: 2px;
    margin-bottom: 28px;
  }

  .step-item {
    height: 42px;
    gap: 6px;
    font-size: 12px;
    letter-spacing: 0;
    clip-path: polygon(0 0, calc(100% - 12px) 0, 100% 50%, calc(100% - 12px) 100%, 0 100%, 12px 50%);
  }

  .step-item:first-child {
    clip-path: polygon(0 0, calc(100% - 12px) 0, 100% 50%, calc(100% - 12px) 100%, 0 100%);
  }

  /* 小屏把光晕收小，减少大面积模糊的开销 */
  .glow {
    width: 300px;
    height: 300px;
    filter: blur(90px);
  }

  .done-icon {
    width: 66px;
    height: 66px;
    font-size: 30px;
  }
}

/* 手机 */
@media (max-width: 480px) {
  .forget-page {
    padding: 18px 12px;
  }

  .forget-inner {
    padding: 22px 16px;
    border-radius: 12px;
  }

  .page-title {
    margin-bottom: 20px;
    font-size: 21px;
    letter-spacing: 2px;
  }

  .step-bar {
    margin-bottom: 24px;
  }

  .step-item {
    height: 38px;
    gap: 4px;
    font-size: 11px;
    clip-path: polygon(0 0, calc(100% - 9px) 0, 100% 50%, calc(100% - 9px) 100%, 0 100%, 9px 50%);
  }

  .step-item:first-child {
    clip-path: polygon(0 0, calc(100% - 9px) 0, 100% 50%, calc(100% - 9px) 100%, 0 100%);
  }

  .step-title {
    font-size: 18px;
    letter-spacing: 2px;
  }

  .step-tip {
    margin-bottom: 22px;
    font-size: 13px;
  }

  .form-item {
    margin-bottom: 22px;
  }

  /* 手机上也保持一行，按钮再收窄一点，给输入框留够宽度 */
  .code-row {
    gap: 8px;
  }

  .code-btn {
    width: 108px;
    padding: 0 6px;
    font-size: 12px;
  }

  /* 第二步的两个按钮竖排，主按钮在上 */
  .btn-row {
    flex-direction: column-reverse;
    gap: 12px;
  }

  .btn-row .submit-btn,
  .ghost-btn {
    width: 100%;
    min-width: 0;
    padding: 0;
  }
}
</style>
