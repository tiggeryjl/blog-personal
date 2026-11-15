<template>
  <div id="captcha-container" style="display: none"></div>
  <button id="captcha-trigger" type="button" style="display: none">触发验证</button>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';

const emit = defineEmits(['captchaSuccess', 'captchaFail']);
let captchaInstance = null;
let verifySucceeded = false;
let closeTimer = null;
let initError = '';
const instanceWaiters = new Set();
const customWidth = 360;
const INSTANCE_WAIT_TIMEOUT = 8000;

const clearCloseTimer = () => {
  if (closeTimer) {
    clearTimeout(closeTimer);
    closeTimer = null;
  }
};

const emitFail = (type, message) => {
  if (verifySucceeded) return;
  clearCloseTimer();
  emit('captchaFail', { type, message });
};

const failInit = (message) => {
  initError = message;
  for (const waiter of instanceWaiters) {
    clearTimeout(waiter.timer);
    waiter.reject(new Error(message));
  }
  instanceWaiters.clear();
};

const waitForInstance = () => {
  if (captchaInstance && typeof captchaInstance.show === 'function') {
    return Promise.resolve(captchaInstance);
  }
  if (initError) {
    return Promise.reject(new Error(initError));
  }

  return new Promise((resolve, reject) => {
    const waiter = {
      resolve,
      reject,
      timer: null,
    };
    waiter.timer = setTimeout(() => {
      instanceWaiters.delete(waiter);
      reject(new Error('安全验证组件加载超时，请检查网络后刷新页面'));
    }, INSTANCE_WAIT_TIMEOUT);
    instanceWaiters.add(waiter);
  });
};

// 验证成功回调
const onSuccess = (result) => {
  // console.log('阿里云返回结果:', result);
  if (result) {
    clearCloseTimer();
    verifySucceeded = true;
    emit('captchaSuccess', result);
  } else {
    console.error('未获取到 CaptchaVerifyParam,请检查场景ID配置');
    emitFail('error', '验证参数为空，请刷新页面重试');
  }
};

// 验证失败回调
const onFail = (error) => {
  if (verifySucceeded) return;
  console.error('滑块验证失败', error);
  emitFail('fail', '安全验证未通过，请重试');
};

// 获取实例回调
const onGetInstance = (instance) => {
  captchaInstance = instance;
  initError = '';
  for (const waiter of instanceWaiters) {
    clearTimeout(waiter.timer);
    waiter.resolve(instance);
  }
  instanceWaiters.clear();
};

const onerror = (err) => {
  if (verifySucceeded) return;
  console.error('验证码组件异常', err);
  const message = typeof err?.msg === 'string' ? err.msg : '安全验证加载失败，请刷新页面重试';
  failInit(message);
  emitFail('error', message);
};

const onClose = () => {
  // console.log('用户关闭验证码弹窗');
  if (verifySucceeded) return;
  // SDK 在验证成功时可能先关闭弹窗、后触发 success，延迟确认避免误报取消
  clearCloseTimer();
  closeTimer = setTimeout(() => {
    closeTimer = null;
    if (!verifySucceeded) {
      emit('captchaFail', { type: 'close', message: '' });
    }
  }, 350);
};

// 初始化验证码
// 注意：只能在 onMounted 里调用，不能在 setup 顶层调用
// const 声明存在暂时性死区，声明前调用会抛 ReferenceError 导致整个登录页白屏
const initCaptcha = (rem) => {
  const config = {
    SceneId: 'jvdry8hq',
    mode: 'popup',
    element: '#captcha-container',
    button: '#captcha-trigger',
    success: onSuccess,
    fail: onFail,
    getInstance: onGetInstance,
    onError: onerror,
    onClose: onClose,
    delayBeforeSuccess: false,
    slideStyle: {
      width: customWidth,
      height: 40,
    },
    rem: rem,
  };
  if (typeof window.initAliyunCaptcha === 'function') {
    Promise.resolve(window.initAliyunCaptcha(config)).catch((error) => {
      console.error('验证码初始化失败', error);
      const message = '安全验证初始化失败，请刷新页面重试';
      failInit(message);
      emitFail('error', message);
    });
  } else {
    console.error('阿里云验证码 SDK 未加载，请检查 index.html');
    failInit('阿里云验证码 SDK 未加载，请刷新页面重试');
  }
};

// 暴露给父组件的方法：主动弹出验证码
const show = async () => {
  clearCloseTimer();
  verifySucceeded = false;
  try {
    const instance = await waitForInstance();
    if (typeof instance.show !== 'function') {
      throw new Error('安全验证实例不可用，请刷新页面重试');
    }
    captchaInstance.show();
  } catch (error) {
    console.error('验证码实例未就绪', error);
    emit('captchaFail', {
      type: 'error',
      message: error?.message || '安全验证组件未加载，请刷新页面重试',
    });
  }
};

defineExpose({ show });

onMounted(() => {
  // 窄屏手机按屏宽换算 rem，让滑块验证码尺寸正常；宽屏用 SDK 默认值
  const pageWidth = window.innerWidth;
  const rem = pageWidth <= customWidth ? Math.floor((pageWidth / customWidth) * 100) / 100 : undefined;
  initCaptcha(rem);
});

onUnmounted(() => {
  clearCloseTimer();
  for (const waiter of instanceWaiters) {
    clearTimeout(waiter.timer);
    waiter.reject(new Error('安全验证组件已关闭'));
  }
  instanceWaiters.clear();
  if (captchaInstance && typeof captchaInstance.destroy === 'function') {
    captchaInstance.destroy();
  }
});
</script>
