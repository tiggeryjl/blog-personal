<template>
  <div class="live2d-wrapper">
    <vue-live2d :model="model" :width="260" :height="350" :cache="true" @click="onClick" />
    <div v-show="showText" class="dialog">{{ text }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VueLive2d from 'vue-live2d'

// 🔥 国内高速CDN + 轻量模型 = 秒加载
const model = ref([
  "https://fastly.jsdelivr.net/npm/live2d-widget-model-shizuku/assets/shizuku.model.json"
])

const showText = ref(false)
const text = ref('')

const onClick = () => {
  const list = ['你好呀～', '不要点我啦', '我会一直陪着你', '今天超开心～']
  text.value = list[Math.floor(Math.random() * list.length)]
  showText.value = true
  setTimeout(() => (showText.value = false), 2500)
}
</script>

<style scoped>
.live2d-wrapper {
  position: fixed;
  left: 20px;
  bottom: 0px;
  z-index: 9999;
}

.dialog {
  position: absolute;
  bottom: 90%;
  left: 50%;
  /* transform: translateX(-50%); */
  background: #fff;
  padding: 6px 12px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  margin-left: -70px;
}
</style>