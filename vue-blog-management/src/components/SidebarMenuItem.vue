<script setup>
// 递归菜单组件
import SidebarMenuItem from './SidebarMenuItem.vue';

defineProps({
  item: { type: Object, required: true },
});
</script>

<template>
  <!-- 有子菜单：渲染成可展开的下拉菜单 -->
  <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
    <template #title>
      <el-icon v-if="item.meta?.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <span>{{ item.meta?.title }}</span>
    </template>
    <SidebarMenuItem v-for="child in item.children" :key="child.path" :item="child" />
  </el-sub-menu>

  <!-- 没有子菜单：叶子节点，点击跳转 -->
  <el-menu-item v-else :index="item.path">
    <el-icon v-if="item.meta?.icon">
      <component :is="item.meta.icon" />
    </el-icon>
    <span>{{ item.meta?.title }}</span>
  </el-menu-item>
</template>
