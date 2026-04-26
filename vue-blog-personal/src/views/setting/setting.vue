<script setup>
import { ref } from 'vue'
import { DArrowLeft } from '@element-plus/icons-vue'

// 菜单数据
const menuList = ref([
  { key: 'base', name: '基本设置' },
  { key: 'account', name: '账号安全' },
  { key: 'theme', name: '主题样式' },
  { key: 'notify', name: '消息通知' },
  { key: 'about', name: '关于本站' }
])

// 当前选中
const activeKey = ref('base')
</script>

<template>
  <div class="common-article-detail">
    <!-- 设置页面整体布局 -->
    <div class="settings-layout">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <div v-for="item in menuList" :key="item.key" class="menu-item" :class="{ active: activeKey === item.key }"
          @click="activeKey = item.key">
          {{ item.name }}
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="settings-content">
        <div v-if="activeKey === 'base'">
          <h3>基本设置</h3>
          <p>这里可以修改昵称、头像、简介等基础信息</p>
        </div>

        <div v-if="activeKey === 'account'">
          <h3>账号安全</h3>
          <p>修改密码、绑定邮箱、登录设备管理</p>
        </div>

        <div v-if="activeKey === 'theme'">
          <h3>主题样式</h3>
          <p>深色模式 / 浅色模式、主题色切换</p>
        </div>

        <div v-if="activeKey === 'notify'">
          <h3>消息通知</h3>
          <p>设置邮件通知、站内消息</p>
        </div>

        <div v-if="activeKey === 'about'">
          <h3>关于本站</h3>
          <p>版本信息、项目介绍、联系方式</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.common-article-detail {
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 20px 12px;
  display: flex;
  gap: 30px;
  min-height: calc(100vh - 65px);
  width: 82%;
  margin: 0 auto;
}

.article-detail {
  background-color: var(--card-secound-bg);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  border-radius: 8px;
  padding: 20px 12px;
}

/* ====================== 设置页布局 ====================== */
.settings-layout {
  display: flex;
  gap: 20px;
  width: 100%;
  min-height: 100%;
}

/* 左侧菜单 */
.settings-menu {
  width: 200px;
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  padding: 12px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-color);
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.menu-item.active {
  background-color: var(--primary-color);
  color: #fff;
  font-weight: 500;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  background-color: var(--card-bg);
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