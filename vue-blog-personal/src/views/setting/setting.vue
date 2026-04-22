<script setup>
import { DArrowLeft } from '@element-plus/icons-vue'
</script>
<template>
  <div class="common-article-detail">
    <div class="article-detail">
      设置页面
    </div>
  </div>
</template>

<style scoped>
.common-article-detail {
  color: #ffffff;
  font-size: 50px;
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 65px);
  width: 82%;
  margin: 0 auto;
}

.article-detail {
  background-color: var(--card-bg);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  border-radius: 8px;
  padding: 20px 12px;
}
</style>