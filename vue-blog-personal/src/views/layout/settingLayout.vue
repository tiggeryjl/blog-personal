<script setup>
import { ref, computed } from 'vue'
import { DArrowLeft } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 菜单数据
const menuList = ref([
  { key: '/base', name: '个人信息' },
  { key: '/repwd', name: '修改密码' },
  // { key: '/account', name: '账号安全' },
  // { key: 'notify', name: '消息通知' },
  // { key: 'about', name: '关于本站' }
])

// 当前选中
const activeKey = computed(() => route.path)

const goPage = (path) => {
  router.push(path)
}
</script>

<template>
  <div class="common-article-detail">
    <!-- 设置页面整体布局 -->
    <div class="settings-layout">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <h2>账号与安全</h2>
        <div class="divider"></div>
        <div v-for="item in menuList" :key="item.key" class="menu-item" :class="{ active: activeKey === item.key }"
          @click="goPage(item.key)">
          {{ item.name }}
        </div>
      </div>

      <!-- 右侧内容区 -->
      <main class="settings-content">
        <router-view />
      </main>
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
.divider {
  height: 1px;
  background: var(--border-color);
}

.settings-menu h2 {
  font-size: 20px;
  color: var(--text-color);
}

.settings-menu {
  width: 24%;
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  font-size: 18px;
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
  border-radius: 8px;
  min-height: 100%;
}
</style>