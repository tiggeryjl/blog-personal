<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

// 控制按钮显示/隐藏
const showScrollTop = ref(false)
// 滚动阈值，超过此值显示按钮
const scrollThreshold = 300
// 控制喷射动画状态
const isLaunching = ref(false)

// 节流函数，优化滚动事件性能
const throttle = (fn, delay = 100) => {
  let lastTime = 0
  return (...args) => {
    const now = Date.now()
    if (now - lastTime > delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

// 处理滚动事件
const handleScroll = throttle(() => {
  showScrollTop.value = window.scrollY > scrollThreshold
})

// 滚动到顶部
const scrollToTop = () => {
  // 触发喷射动画
  isLaunching.value = true
  setTimeout(() => {
    isLaunching.value = false
  }, 600)

  // 保存当前滚动位置，用于动画
  const startPosition = window.scrollY
  const startTime = performance.now()
  const duration = 500 // 动画持续时间(ms)

  // 自定义平滑滚动动画
  const animateScroll = (timestamp) => {
    const elapsed = timestamp - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3)

    window.scrollTo(0, startPosition * (1 - easeProgress))

    if (progress < 1) {
      requestAnimationFrame(animateScroll)
    }
  }

  requestAnimationFrame(animateScroll)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <transition name="fade">
    <button v-if="showScrollTop" @click="scrollToTop" class="scroll-top-btn" aria-label="回到页面顶部"
      :class="{ 'launching': isLaunching }">
      <font-awesome-icon icon="rocket" size="2x" color="#fff" class="rocket" />
      <font-awesome-icon icon="wind" class="smoke" />
    </button>
  </transition>
</template>

<style scoped>
.scroll-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  background-color: var(--card-bg);
  border-radius: 50%;
  /* background-color: transparent; */
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: none;
  transition: all 0.3s ease;
  z-index: 500;
}

.scroll-top-btn:hover {
  background-color: var(--card-bg-hover);
  /* background-color: transparent; */
  transform: translateY(-3px);
}

.scroll-top-btn:active {
  transform: translateY(-1px);
}

.rocket {
  color: var(--scroll-top-color);
  transform: rotate(-45deg);
}

.smoke {
  position: absolute;
  left: 18px;
  top: 36px;
  font-size: 10px;
  color: var(--scroll-top-color);
  transform: rotate(90deg);
}

/* 火箭发射抖动 */
.launching {
  animation: rocket-shake 0.6s ease-out;
}

/* 火焰：放大 + 向下移动 */
.launching .smoke {
  animation: wind-blast 0.6s ease-out;
}

@keyframes rocket-shake {

  0%,
  100% {
    transform: translateY(0);
  }

  20% {
    transform: translateY(-6px);
  }

  40% {
    transform: translateY(-12px);
  }

  60% {
    transform: translateY(-8px);
  }

  80% {
    transform: translateY(-4px);
  }
}

@keyframes wind-blast {
  0% {
    transform: rotate(90deg) scale(1) translateX(0);
    opacity: 1;
  }

  50% {
    transform: rotate(90deg) scale(1.3) translateX(3px);
    opacity: 0.8;
  }

  100% {
    transform: rotate(90deg) scale(1) translateX(0);
    opacity: 1;
  }
}

/* 淡入淡出过渡效果 */
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
</style>
