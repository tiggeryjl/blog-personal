<script setup>
// 侧边栏菜单：PC 端侧边栏与移动端抽屉共用同一套菜单渲染逻辑
import { useRoute } from 'vue-router';
import SidebarMenuItem from '@/components/SidebarMenuItem.vue';

defineProps({
  // 过滤后的动态路由（已剔除 meta.hidden 的菜单）
  routes: { type: Array, default: () => [] },
});

const emit = defineEmits(['select']);
const route = useRoute();
</script>

<template>
  <el-menu
    router
    class="sidebar-menu"
    :default-active="route.path"
    :collapse-transition="false"
    @select="emit('select', $event)"
  >
    <template v-for="item in routes" :key="item.path">
      <!-- 多个子菜单：交给递归组件渲染，支持任意层级 -->
      <SidebarMenuItem v-if="item.children?.length > 1" :item="item" />
      <!-- 没有/只有一个子菜单：保持单菜单项效果 -->
      <el-menu-item v-else :index="item.children?.length ? item.children[0].path : item.path">
        <el-icon>
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta.title }}</span>
      </el-menu-item>
    </template>
  </el-menu>
</template>
