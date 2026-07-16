import { usePermissionStore } from '@/stores/permission'

export default {
  install(app) {
    app.directive('perm', {
      mounted(el, binding) {
        const permissionStore = usePermissionStore()
        const permValue = binding.value
        // 没有权限直接移除按钮
        if (!permissionStore.hasPerm(permValue)) {
          el.parentNode && el.parentNode.removeChild(el)
        }
      }
    })
  }
}