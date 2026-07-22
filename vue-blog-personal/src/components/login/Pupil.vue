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
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  size: {
    type: Number,
    default: 12
  },
  maxDistance: {
    type: Number,
    default: 5
  },
  pupilColor: {
    type: String,
    default: 'black'
  },
  forceLookX: {
    type: Number
  },
  forceLookY: {
    type: Number
  }
})


const mx = ref(0), my = ref(0)
const pupilRef = ref(null)
const onMove = (e) => { mx.value = e.clientX; my.value = e.clientY }
onMounted(() => window.addEventListener('mousemove', onMove))
onUnmounted(() => window.removeEventListener('mousemove', onMove))

const pos = computed(() => {
  if (!pupilRef.value) return { x: 0, y: 0 }
  if (props.forceLookX !== undefined && props.forceLookY !== undefined)
    return { x: props.forceLookX, y: props.forceLookY }
  const r = pupilRef.value.getBoundingClientRect()
  const dx = mx.value - (r.left + r.width / 2)
  const dy = my.value - (r.top + r.height / 2)
  const d = Math.min(Math.sqrt(dx ** 2 + dy ** 2), props.maxDistance)
  const a = Math.atan2(dy, dx)
  return { x: Math.cos(a) * d, y: Math.sin(a) * d }
})
</script>

<template>
  <div ref="pupilRef" class="pupil" :style="{
    width: size + 'px', height: size + 'px',
    backgroundColor: pupilColor,
    transform: `translate(${pos.x}px, ${pos.y}px)`,
  }" />
</template>


<style scoped>
.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
}
</style>
