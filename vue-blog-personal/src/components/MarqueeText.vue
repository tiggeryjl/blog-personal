<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  text: { type: String, default: '' },
  // 每个字符对应的滚动秒数，文本越长滚动越慢
  speed: { type: Number, default: 0.18 },
  minDuration: { type: Number, default: 4 },
})

const staticEl = ref(null)
const overflows = ref(false)

// 仅当文本宽度超过容器时才需要滚动
const checkOverflow = () => {
  const el = staticEl.value
  if (!el) return
  // 直接测量文本元素：外层容器会把子元素溢出裁掉，测外层永远不超长
  overflows.value = el.scrollWidth > el.clientWidth + 1
}

const duration = computed(() => {
  const len = (props.text || '').length
  return Math.max(props.minDuration, Math.round(len * props.speed * 10) / 10)
})

const handleResize = () => {
  checkOverflow()
}

onMounted(() => {
  checkOverflow()
  // 字体/图片加载后尺寸可能变化，稍后再校验一次
  setTimeout(checkOverflow, 300)
  window.addEventListener('load', handleResize)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('load', handleResize)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <span
    class="marquee"
    :class="{ scrolling: overflows }"
    :style="{ '--dur': duration + 's' }"
  >
    <span ref="staticEl" class="marquee-static">{{ text }}</span>
    <span v-if="overflows" class="marquee-track">
      <span class="marquee-half">{{ text }}&nbsp;&nbsp;·&nbsp;&nbsp;</span>
      <span class="marquee-half">{{ text }}&nbsp;&nbsp;·&nbsp;&nbsp;</span>
    </span>
  </span>
</template>

<style scoped>
.marquee {
  display: block;
  overflow: hidden;
  white-space: nowrap;
}

.marquee-static {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.marquee-track {
  display: none;
  white-space: nowrap;
  will-change: transform;
}

/* 悬浮时循环滚动 */
.marquee.scrolling:hover .marquee-static {
  display: none;
}

.marquee.scrolling:hover .marquee-track {
  display: inline-flex;
  animation: marquee-loop var(--dur, 6s) linear infinite;
}

/* 滚动轨道左对齐，保证两份文本无缝衔接 */
.marquee.scrolling:hover {
  text-align: left;
}

@keyframes marquee-loop {
  to {
    transform: translateX(-50%);
  }
}
</style>
