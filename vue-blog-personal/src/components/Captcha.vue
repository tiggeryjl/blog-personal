<template>
  <div id="captcha-container" style="display: none;"></div>
  <button id="captcha-trigger" style="display: none;">触发验证</button>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const emit = defineEmits(['captchaSuccess', 'captchaFail'])
let captchaInstance = null
const customWidth = 360;

// 验证成功回调
const onSuccess = (result) => {
  console.log('阿里云返回结果:', result)
  if (result) {
    emit('captchaSuccess', result)
  } else {
    console.error('未获取到 CaptchaVerifyParam,请检查场景ID配置')
    emit('captchaFail')
  }
}

// 验证失败回调
const onFail = (error) => {
  console.error('滑块验证失败', error)
  emit('captchaFail')
}

// 获取实例回调
const onGetInstance = (instance) => {
  captchaInstance = instance
}

const onerror = (err) => {
  console.error('验证码错误（包括用户关闭）', err)
  emit('captchaFail')
}

const onClose = (err) => {
  console.log('用户关闭验证码弹窗')
  emit('captchaFail')
}

const pageWidth = window.innerWidth;
if (pageWidth <= customWidth) {
  const rem = Math.floor(pageWidth / customWidth * 100) / 100;
  initCaptcha(rem);
}

// 初始化验证码
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
    slideStyle: {
      width: customWidth,
      height: 40,
    },
    rem: rem,
  }
  if (typeof window.initAliyunCaptcha === 'function') {
    window.initAliyunCaptcha(config)
  } else {
    console.error('阿里云验证码 SDK 未加载，请检查 index.html')
  }
}

// 暴露给父组件的方法：主动弹出验证码
const show = () => {
  if (captchaInstance && typeof captchaInstance.show === 'function') {
    captchaInstance.show()
  } else {
    console.warn('验证码实例未就绪')
    emit('captchaFail')
  }
}

defineExpose({ show })

onMounted(() => {
  initCaptcha()
})

onUnmounted(() => {
  if (captchaInstance && typeof captchaInstance.destroy === 'function') {
    captchaInstance.destroy()
  }
})
</script>