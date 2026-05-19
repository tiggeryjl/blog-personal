<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'

const router = useRouter()
//倒计时5秒
const countTime = ref(5)
let timer = null

// 返回首页
const goHome = () => {
  router.push('/')
}


// 倒计时逻辑
const startCountDown = () => {
  timer = setInterval(() => {
    countTime.value--
    // 倒计时结束
    if (countTime.value <= 0) {
      clearInterval(timer)
      goHome()
    }
  }, 1000)
}

onMounted(() => {
  startCountDown()
})

// 页面销毁时清除定时器（防止bug）
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

</script>

<template>
  <div class="not-found-page">
    <div class="container">
      <h1 class="code">404</h1>

      <!-- 提示文字 -->
      <div class="text-group">
        <h2>哎呀！页面不见了</h2>
        <p>你访问的资源不存在，或该资源正在规划中...</p>
        <p class="jump-tip">⏳ <span>{{ countTime }}</span>秒后将自动返回首页，也可点击按钮立即返回</p>
      </div>

      <!-- 操作按钮 -->
      <div class="btn-group">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="router.back()">返回上一页</el-button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 65px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--card-bg);
  padding: 20px;
  animation: fadeIn 0.8s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.container {
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.code {
  font-size: 120px;
  margin: 0;
  color: var(--primary-color);
  font-weight: 900;
  opacity: 0.8;
  animation: breath 2s ease-in-out infinite;
}

@keyframes breath {
  0% {
    opacity: 0.7;
    transform: scale(0.95);
  }

  50% {
    opacity: 1;
    transform: scale(1.05);
  }

  100% {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.text-group {
  margin: 20px 0 40px;
}

.text-group h2 {
  font-size: 24px;
  color: var(--text-color);
  margin-bottom: 12px;
}

.text-group p {
  font-size: 16px;
  color: var(--text-secondary-color);
  line-height: 1.6;
  margin: 6px 0;
}

.jump-tip {
  font-size: 14px;
  margin-top: 15px;
}

.jump-tip span {
  color: var(--primary-color);
  font-weight: bold;
  font-size: 20px;
  display: inline-block;
  animation: scale 1s infinite;
}

@keyframes scale {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.2);
  }

  100% {
    transform: scale(1);
  }
}

.btn-group {
  display: flex;
  gap: 16px;
  justify-content: center;
  animation: fadeInBtn 1.2s ease-in-out;
}

@keyframes fadeInBtn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

:deep(.el-button) {
  padding: 10px 24px;
  font-size: 15px;
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>