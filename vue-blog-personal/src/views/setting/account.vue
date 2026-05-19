<script setup>
import { ref } from 'vue'
import { DArrowLeft } from '@element-plus/icons-vue'

// 当前选中
const activeKey = ref('account')
</script>

<template>
  <div class="common-article-detail">
    <!-- 右侧内容区 -->
    <div class="settings-content">
      <div>
        <h3>账号安全1111</h3>
        <p>修改密码、绑定邮箱、登录设备管理</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.common-article-detail {
  flex: 1;
  background-color: var(--card-bg);
  border-radius: 8px;
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 65px);
  width: 82%;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  border-radius: 8px;
  padding: 24px;
  min-height: 100%;
}

.settings-content h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  color: var(--text-color);
}

.settings-content p {
  color: var(--text-secondary-color);
  line-height: 1.6;
}
</style>