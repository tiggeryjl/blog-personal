import { defineStore } from 'pinia';
import { getUserInfoApi } from '@/api/admin';
import { useUserStore } from '@/stores/userloginstatus';
import router from '@/router';
import { filterAsyncRoutes } from '@/utils/route';

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 当前用户权限标识数组：['sys:user:add','sys:user:edit']
    permissionList: [],
    // 后端返回动态菜单路由
    dynamicRoutes: [],
    // 已挂载的动态路由记录，防止重复添加
    addRouteNames: [],
    isFetchPermission: false,
    roles: [],
  }),
  actions: {
    /**
     * 拉取用户权限、菜单
     */
    async getUserPermission() {
      // this.addRouteNames.forEach(name => {
      //   router.removeRoute(name)
      // })
      // this.addRouteNames = []
      const oldNames = [...this.addRouteNames];
      this.addRouteNames = [];
      oldNames.forEach((name) => {
        try {
          router.removeRoute(name);
        } catch (e) {}
      });
      console.log('开始获取权限...');
      const result = await getUserInfoApi();
      console.log(result.data.permissions);
      useUserStore().updateUserInfo(result.data.user);
      this.permissionList = result.data.permissions || [];
      const routes = filterAsyncRoutes(result.data.routers || []);
      this.dynamicRoutes = routes;
      routes.forEach((route) => {
        router.addRoute(route);
        this.addRouteNames.push(route.name);
      });
      router.addRoute({
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
      });

      this.isFetchPermission = true;
      this.roles = result.data.roles || [];
      return result;
    },

    /**
     * 按钮权限校验方法
     * @param {string} perm 权限标识
     * @returns boolean
     */
    hasPerm(perm) {
      if (!perm) return true;
      // 超级管理员直接放行
      if (this.roles.includes('admin')) return true;
      return this.permissionList.includes(perm);
    },

    /**
     * 清空权限，退出登录调用
     */
    resetPermission() {
      // 删除已挂载的动态路由
      this.addRouteNames.forEach((name) => {
        router.removeRoute(name);
      });
      this.$reset();
    },
  },
  persist: false,
});
