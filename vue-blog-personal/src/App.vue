<script setup>
import { initTheme } from './utils/theme'
import { onMounted } from 'vue'
import { useUserStore } from '@/store/userloginstatus'

initTheme()

onMounted(() => {
  const userStore = useUserStore()
  userStore.loadStorage() // 一进项目就恢复登录状态
})
</script>

<template>
  <router-view></router-view>
</template>

<style scoped></style>
