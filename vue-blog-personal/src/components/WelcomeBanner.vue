<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ArrowDownBold } from '@element-plus/icons-vue'

const banners = ref([
  {
    url: 'https://picsum.photos/1920/1080?random=banner1',
    title: 'Hello! 欢迎来到 我的世界!',
    quote: '不管你说再多的慌，只有自己的内心，是无法欺骗的啊。'
  }
])
const scrollToContent = () => {
  window.scrollTo({
    top: window.innerHeight,
    behavior: 'smooth'
  })
}
// setInterval(() => {
//   current.value = (current.value + 1) % banners.value.length
// }, 5000)
const current = ref(0)
const scrollY = ref(0)
const handleScroll = () => {
  scrollY.value = window.scrollY
}

const displayQuote = ref('')
let typingTimer = null

const typeText = (text, target, delay = 80) => {
  let index = 0
  target.value = ''

  clearTimeout(typingTimer)
  typingTimer = setTimeout(() => {
    const interval = setInterval(() => {
      if (index >= text.length) {
        clearInterval(interval)
        return
      }
      target.value = text.slice(0, index + 1)
      index++
    }, delay)
  }, 300)
}

onMounted(() => {
  typeText(banners.value[current.value].quote, displayQuote, 50)

  window.addEventListener('scroll', () => {
    scrollY.value = window.scrollY
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', () => {
    scrollY.value = window.scrollY
  })
  clearTimeout(typingTimer)
})

const bannerOpacity = () => {
  const opacity = 1 - scrollY.value / 200
  return Math.max(0, Math.min(1, opacity))
}

const bannerTranslate = () => {
  return scrollY.value * 0.3
}
</script>

<template>
  <div class="welcome-banner" :style="{
    opacity: bannerOpacity(),
    transform: `translateY(${-bannerTranslate()}px)`
  }">
    <!-- <div class="banner-bg" :style="{ backgroundImage: `url(${banners[current].url})` }"></div> -->
    <div class="banner-mask"></div>
    <div class="meteor-shower">
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
      <div class="meteor"></div>
    </div>

    <div class="banner-overlay">
      <h1 class="banner-title">{{ banners[current].title }}</h1>
      <p class="banner-quote">{{ displayQuote }}<span class="cursor">|</span></p>
    </div>

    <div class="scroll-arrow" @click="scrollToContent">
      <el-icon>
        <ArrowDownBold />
      </el-icon>
    </div>

    <div class="banner-animation"></div>
  </div>
</template>

<style scoped>
.welcome-banner {
  position: relative;
  width: 100vw;
  height: 100vh;
  margin-top: -80px;
  padding-top: 65px;
  overflow: hidden;
  /* 加上过渡，让变化更丝滑 */
  transition: opacity 0.1s ease-out, transform 0.1s ease-out;
  margin-left: calc(-50vw + 50%);
}

.banner-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: brightness(0.7);
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  /* 透明度 0.4，越大越黑 */
  z-index: 0;
}

/* 流星雨容器 */
.meteor-shower {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

/* 单个流星 —— 指向左下角 */
.meteor {
  position: absolute;
  width: 150px;
  height: 2px;
  background: linear-gradient(to right, rgba(242, 255, 0, 0.8), rgba(255, 255, 255, 0));
  transform: rotate(40deg);
  /* 🔥 关键：改成负角度，向左下角 */
  opacity: 0;
  animation: meteor 3s infinite linear;
}

/* 随机位置 + 延迟 */
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
    /* 🔥 向左下移动 */
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
</style>