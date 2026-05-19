<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowDownBold } from '@element-plus/icons-vue'

const BANNERS = [{
  title: 'Hello! 欢迎来到 我的世界!',
  quote: [
    '不管你说再多的慌，只有自己的内心，是无法欺骗的啊。',
    '生活原本沉闷，但跑起来就有风。',
    '保持热爱，奔赴下一场山海。',
    '你逆光而来，配得上这世间所有的好。',
    '努力的意义，就是以后的日子里，放眼望去，全部都是自己喜欢的人和事。'
  ]
}]

// 滚动配置
const SCROLL_OPACITY_DISTANCE = 200
const SCROLL_TRANSLATE_FACTOR = 0.3
const TYPING_DELAY = 50
const TYPING_INTERVAL = 80

// 响应式变量
const current = ref(0)
const scrollY = ref(0)
const displayQuote = ref('')

// 定时器统一管理
let typingInterval = null
let typingTimeout = null

const bannerOpacity = computed(() => {
  const opacity = 1 - scrollY.value / SCROLL_OPACITY_DISTANCE
  return Math.max(0, Math.min(1, opacity))
})

const bannerTranslate = computed(() => {
  return scrollY.value * SCROLL_TRANSLATE_FACTOR
})

const getRandomQuote = (list) => {
  return list[Math.floor(Math.random() * list.length)]
}

// 打字机效果
const typeText = (text) => {
  clearInterval(typingInterval)
  clearTimeout(typingTimeout)

  displayQuote.value = ''
  let index = 0

  typingTimeout = setTimeout(() => {
    typingInterval = setInterval(() => {
      if (index >= text.length) {
        clearInterval(typingInterval)
        return
      }
      displayQuote.value = text.slice(0, index + 1)
      index++
    }, TYPING_INTERVAL)
  }, TYPING_DELAY)
}

const handleScroll = () => {
  scrollY.value = window.scrollY
}

const scrollToContent = () => {
  const navHeight = window.innerWidth <= 768 ? 60 : 80
  window.scrollTo({
    top: window.innerHeight - navHeight,
    behavior: 'smooth'
  })
}

onMounted(() => {
  const randomText = getRandomQuote(BANNERS[current.value].quote)
  typeText(randomText)

  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  clearInterval(typingInterval)
  clearTimeout(typingTimeout)
})
</script>

<template>
  <div class="welcome-banner" :style="{
    opacity: bannerOpacity,
    transform: `translateY(${-bannerTranslate}px)`
  }">
    <div class="banner-mask" />
    <div class="meteor-shower">
      <div class="meteor" v-for="i in 8" :key="i" />
    </div>

    <div class="banner-overlay">
      <h1 class="banner-title">{{ BANNERS[current].title }}</h1>
      <p class="banner-quote">
        {{ displayQuote }}
        <span class="cursor">|</span>
      </p>
    </div>

    <div class="scroll-arrow" @click="scrollToContent">
      <el-icon>
        <ArrowDownBold />
      </el-icon>
    </div>

    <div class="banner-animation" />
  </div>
</template>

<style scoped>
.welcome-banner {
  position: relative;
  width: 100%;
  height: 100vh;
  margin-top: -80px;
  padding-top: 65px;
  overflow: hidden;
  transition: opacity 0.15s ease-out, transform 0.15s ease-out;
  will-change: opacity, transform;
  /* 浏览器性能优化 */
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 0;
}

/* 流星雨 */
.meteor-shower {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.meteor {
  position: absolute;
  width: 150px;
  height: 2px;
  background: linear-gradient(to right, rgba(242, 255, 0, 0.8), rgba(255, 255, 255, 0));
  transform: rotate(-40deg);
  opacity: 0;
  animation: meteor 3s infinite linear;
}

.meteor:nth-child(1) {
  top: 5%;
  left: 80%;
  animation-delay: 0s;
}

.meteor:nth-child(2) {
  top: 15%;
  left: 90%;
  animation-delay: 0.5s;
}

.meteor:nth-child(3) {
  top: 25%;
  left: 70%;
  animation-delay: 1s;
}

.meteor:nth-child(4) {
  top: 5%;
  left: 60%;
  animation-delay: 1.5s;
}

.meteor:nth-child(5) {
  top: 20%;
  left: 50%;
  animation-delay: 2s;
}

.meteor:nth-child(6) {
  top: 35%;
  left: 85%;
  animation-delay: 2.5s;
}

.meteor:nth-child(7) {
  top: 40%;
  left: 75%;
  animation-delay: 1.2s;
}

.meteor:nth-child(8) {
  top: 50%;
  left: 65%;
  animation-delay: 0.8s;
}

@keyframes meteor {
  0% {
    transform: translate(0, 0) rotate(-40deg);
    opacity: 0;
  }

  10% {
    opacity: 1;
  }

  90% {
    opacity: 0.6;
  }

  100% {
    transform: translate(-200px, 180px) rotate(-40deg);
    opacity: 0;
  }
}

.banner-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  z-index: 1;
}

.banner-title {
  font-size: 36px;
  margin: 0 0 16px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.banner-quote {
  font-size: 18px;
  margin: 0;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.5);
  min-height: 1.5em;
  /* 防止布局抖动 */
}

.cursor {
  animation: blink 1s infinite;
  margin-left: 4px;
}

@keyframes blink {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0;
  }
}

.scroll-arrow {
  position: absolute;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  z-index: 10;
  cursor: pointer;
  animation: bounce 2s infinite;
  font-size: 38px;
  color: #fff;
}

@keyframes bounce {

  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }

  50% {
    transform: translateX(-50%) translateY(28px);
  }
}

.banner-animation {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 140px;
  background: linear-gradient(to top, var(--card-bg), transparent);
  z-index: 9;
}

/* 响应式 */
@media (max-width: 1024px) {
  .banner-title {
    font-size: 30px;
    padding: 0 15px;
  }

  .banner-quote {
    font-size: 16px;
    padding: 0 15px;
  }

  .scroll-arrow {
    font-size: 34px;
    bottom: 35px;
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    margin-top: -60px;
    padding-top: 50px;
  }

  .banner-title {
    font-size: 24px;
  }

  .banner-quote {
    font-size: 14px;
  }

  .scroll-arrow {
    font-size: 30px;
    bottom: 30px;
  }

  .meteor {
    width: 100px;
  }
}

@media (max-width: 480px) {
  .banner-title {
    font-size: 20px;
  }

  .banner-quote {
    font-size: 13px;
  }

  .scroll-arrow {
    font-size: 26px;
    bottom: 25px;
  }

  .meteor {
    width: 80px;
  }
}
</style>