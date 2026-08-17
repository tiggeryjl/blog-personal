<script setup>
import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * 仅查看提示：当前角色若没有任何操作权限（只有查询权限），
 * 页面顶部显示友好提示，避免误以为页面无权限。
 * props.perms：该页面所有操作按钮的权限标识数组
 */
const props = defineProps({
  perms: { type: Array, required: true },
})

const permissionStore = usePermissionStore()
const viewOnly = computed(() => !props.perms.some((p) => permissionStore.hasPerm(p)))
</script>

<template>
  <el-alert
    v-if="viewOnly"
    type="info"
    :closable="false"
    show-icon
    title="当前角色仅有查看权限，操作功能不可用"
    style="margin-bottom: 15px"
  />
</template>
