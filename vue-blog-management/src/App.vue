<script setup>
import { onBeforeUnmount, onMounted } from 'vue';
import { initResponsiveTables } from '@/utils/responsiveTable';

let disposeResponsiveTables = null;

onMounted(() => {
  disposeResponsiveTables = initResponsiveTables();
});

onBeforeUnmount(() => {
  disposeResponsiveTables?.();
});
</script>

<template>
  <router-view></router-view>
</template>

<style scoped></style>
