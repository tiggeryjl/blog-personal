<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { unsubscribeRssApi } from '@/api/rss.js';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const success = ref(false);
const message = ref('正在处理退订请求…');

onMounted(async () => {
  const token = route.query.token;
  if (!token) {
    loading.value = false;
    success.value = false;
    message.value = '退订链接不完整，缺少令牌';
    return;
  }

  try {
    const result = await unsubscribeRssApi(token);
    success.value = result.code === 200;
    message.value = result.data || result.msg || '退订处理完成';
  } catch (error) {
    success.value = false;
    message.value = '退订失败，请稍后再试或联系站长';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="unsubscribe-page">
    <div class="unsubscribe-card">
      <h2>邮箱订阅退订</h2>
      <p class="unsubscribe-message" :class="{ success, fail: !loading && !success }">
        {{ message }}
      </p>
      <button class="unsubscribe-btn" @click="router.push('/index')">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.unsubscribe-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  background-color: var(--background-color, #f5f7fa);
}

.unsubscribe-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 28px;
  border-radius: 12px;
  background-color: var(--card-bg, #fff);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.unsubscribe-card h2 {
  margin: 0 0 16px;
  font-size: 20px;
  color: var(--text-color, #303133);
}

.unsubscribe-message {
  margin: 0 0 24px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary-color, #909399);
}

.unsubscribe-message.success {
  color: #67c23a;
}

.unsubscribe-message.fail {
  color: #f56c6c;
}

.unsubscribe-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 999px;
  background-color: #409eff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.unsubscribe-btn:hover {
  background-color: #2f88e0;
}
</style>
