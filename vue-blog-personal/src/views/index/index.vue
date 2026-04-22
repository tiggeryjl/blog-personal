<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'

const router = useRouter()
function goDetail(id) {
  router.push('/article/' + id)
}

const layoutMode = ref(localStorage.getItem('blogLayout') || 'list')

function updateLayout() {
  layoutMode.value = localStorage.getItem('blogLayout') || 'list'
}

onMounted(() => {
  window.addEventListener('layoutChange', updateLayout)
})

onUnmounted(() => {
  // 必须移除监听，避免页面卸载后还在执行
  window.removeEventListener('layoutChange', updateLayout)
})


</script>

<template>
  <div class="blog-layout">
    首页
  </div>
</template>

<style scoped>
.blog-layout {
  padding: 30px;
  color: #ffffff;
  font-size: 50px;
}
</style>