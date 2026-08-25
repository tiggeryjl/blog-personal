import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/userloginstatus';
import { usePermissionStore } from '@/stores/permission';
import { ElMessage } from 'element-plus';
import { getRefreshTokenApi } from '@/api/admin.js';
import LayoutView from '@/views/layout/index.vue';
import IndexView from '@/views/index/index.vue';
import editInputView from '@/views/artwork/articleWork/editInput.vue';
import LoginView from '@/views/login/index.vue';
import ArticleDetailView from '@/views/artwork/articleWork/articleDetail.vue';
import NoticeView from '@/views/noticeCenter/noticeCenter.vue';

export const constantRoutes = [
  {
    path: '',
    name: '',
    component: LayoutView,
    redirect: '/index',
    children: [
      { path: 'index', name: 'index', component: IndexView },
      { path: 'editInput', name: 'editInput', component: editInputView },
      { path: '/notice', name: 'notice', component: NoticeView },
    ],
  },
  { path: '/articleDetail', name: 'articleDetail', component: ArticleDetailView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/403', name: '403', component: () => import('@/views/error/403.vue') },
  // { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/404.vue') }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
});

// 全局标记，防止路由守卫并发静默刷新
let isRefreshing = false;

// 全局前置路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const permissionStore = usePermissionStore();

  const whiteList = ['/login', '/403', '/404'];
  if (whiteList.includes(to.path)) {
    next();
    return;
  }

  // 已登录，存在token
  if (userStore.token) {
    // 已经登录禁止再进入登录页
    if (to.path === '/login') {
      next('/');
      return;
    }
    // 第一次进入系统，还未拉取权限和动态路由
    if (!permissionStore.isFetchPermission) {
      try {
        // 请求后端获取当前用户动态路由、权限标识、角色
        await permissionStore.getUserPermission();
        // 刷新一次路由放行，防止路由白屏
        next({ ...to, replace: true });
      } catch (err) {
        console.error('获取权限失败:', err);
        permissionStore.setFetchPermission(true);
        // 判断是否为 401（未授权）
        if (err.response?.status === 401) {
          await userStore.logout();
          ElMessage.warning('登录已失效，请重新登录');
          next('/login');
        } else {
          ElMessage.error('服务器异常，请稍后刷新重试!');
          // 其他错误
          next(from);
        }
      }
    } else {
      if (to.matched.length > 0) {
        next();
      } else {
        next('/403');
      }
    }
  } else {
    // 无token，执行静默刷新
    if (isRefreshing) {
      next();
      return;
    }
    isRefreshing = true;
    try {
      // 自动携带后端HttpOnly Cookie里的refreshToken
      const result = await getRefreshTokenApi();
      const newToken = result.data.token;
      // 存入pinia
      userStore.setToken(newToken);
      // 刷新token成功后，重新走权限逻辑
      if (!permissionStore.isFetchPermission) {
        await permissionStore.getUserPermission();
        next({ ...to, replace: true });
      } else {
        next();
      }
    } catch (refreshErr) {
      // 刷新失败：refreshToken过期/不存在，强制去登录
      console.log('静默刷新token失败', refreshErr);
      await userStore.logout();
      ElMessage.info('登录过期，请重新登录');
      next('/login');
    } finally {
      isRefreshing = false;
    }
  }
});

// 页面回到顶部
router.afterEach(() => {
  window.scrollTo(0, 0);
});

export default router;
