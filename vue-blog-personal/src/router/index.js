import { createRouter, createWebHistory } from 'vue-router';
import { getRefreshTokenApi, queryUserInfoApi } from '@/api/auth.js';
import { ElMessage } from 'element-plus';
import LayoutView from '@/views/layout/index.vue';
import IndexLayoutView from '@/views/layout/indexLayout.vue';
import SettingLayoutView from '@/views/layout/settingLayout.vue';
import IndexView from '@/views/index/index.vue';
import ArticleView from '@/views/article/article.vue';
import ArticleDetailView from '@/views/article/articleDetail.vue';
import DailyDetailView from '@/views/daily/dailyDetail.vue';
import DailyView from '@/views/daily/daily.vue';
import FriendLinkView from '@/views/friendlink/friendlink.vue';
import FeedbackView from '@/views/feedback/feedback.vue';
import AboutView from '@/views/about/about.vue';
import BaseView from '@/views/setting/base.vue';
import RepwdView from '@/views/setting/repwd.vue';
import AccountView from '@/views/setting/account.vue';
import { useUserStore } from '@/store/userloginstatus.js';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    // 保留浏览器前进/后退的滚动位置
    if (savedPosition) return savedPosition;
    else return { top: 0 };
  },
  routes: [
    {
      path: '',
      name: '',
      component: LayoutView,
      redirect: '/index', //重定向，如果访问的路径是 / 这个空路径，那么默认就会重定向到此设置的路径/index
      children: [
        {
          path: '',
          name: '',
          component: IndexLayoutView,
          redirect: '/index',
          children: [
            { path: 'index', name: 'index', component: IndexView },
            { path: 'article', name: 'article', component: ArticleView },
            { path: 'daily', name: 'daily', component: DailyView },
          ],
        },
        {
          path: 'SettingLayout',
          name: 'SettingLayout',
          component: SettingLayoutView,
          redirect: 'base',
          // 个人中心相关页面必须登录后才能访问
          meta: { requiresAuth: true },
          children: [
            { path: '/base', name: 'base', component: BaseView, meta: { requiresAuth: true } },
            { path: '/repwd', name: 'repwd', component: RepwdView, meta: { requiresAuth: true } },
            { path: '/account', name: 'account', component: AccountView, meta: { requiresAuth: true } },
          ],
        },
        { path: 'friendlink', name: 'friendlink', component: FriendLinkView },
        { path: 'feedback', name: 'feedback', component: FeedbackView },
        { path: 'about', name: 'about', component: AboutView },
        { path: 'article/:id', name: 'articleDetail', component: ArticleDetailView },
        { path: 'daily/:id', name: 'dailyDetail', component: DailyDetailView },
      ],
    },
    { path: '/login', name: 'login', component: () => import('@/views/login/index.vue') },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/components/404.vue') },
  ],
});

// 静默恢复登录态：本地没有 token 时，尝试用 cookie 里的 refreshToken 换新 token
// 只尝试一次，失败后不再重试，避免游客每次跳转都发请求
let restorePromise = null;
let restoreFailed = false;

const restoreSession = () => {
  if (restorePromise) return restorePromise;
  if (restoreFailed) return Promise.reject(new Error('未登录'));

  restorePromise = (async () => {
    const res = await getRefreshTokenApi({ silent: true });
    const newToken = res?.code === 200 ? res.data?.token : null;
    if (!newToken) throw new Error('刷新token失败');
    useUserStore().setToken(newToken);
    const result = await queryUserInfoApi();
    useUserStore().updateUserInfo(result.data);
    return true;
  })();

  // 失败时静默处理
  restorePromise.catch(() => {
    restoreFailed = true;
    restorePromise = null;
  });

  return restorePromise;
};

// 主动退出登录后调用：本次会话内不再用 cookie 静默恢复登录态
export const stopSessionRestore = () => {
  restoreFailed = true;
  restorePromise = null;
};

// 公开页面（首页、文章、日常、友链、留言、关于等）游客可直接浏览
// 只有设置 / 个人中心相关页面（meta.requiresAuth）才必须先登录
router.beforeEach(async (to, from, next) => {
  const needLogin = to.matched.some((record) => record.meta?.requiresAuth);

  // 已有本地 token，直接放行
  if (localStorage.getItem('user_token')) return next();

  if (!needLogin) {
    // 游客直接浏览，同时后台静默尝试恢复登录态（有登录态就显示头像，没有就忽略）
    restoreSession().catch(() => {});
    return next();
  }

  // 需要登录的页面：先尝试静默恢复，失败再跳登录页，并记录来源用于登录后回跳
  try {
    await restoreSession();
    next({ ...to, replace: true });
  } catch (err) {
    ElMessage.warning('请先登录后再访问该页面');
    next({ path: '/login', query: { redirect: to.fullPath } });
  }
});

export default router;
