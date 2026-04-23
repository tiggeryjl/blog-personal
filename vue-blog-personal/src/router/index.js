import { createRouter, createWebHistory } from 'vue-router'

import LayoutView from '@/views/layout/index.vue'
import IndexLayoutView from '@/views/index/indexLayout.vue'
import IndexView from '@/views/index/index.vue'
import LoginView from '@/views/login/index.vue'
import ArticleView from '@/views/article/article.vue'
import ArticleDetailView from '@/views/article/articleDetail.vue'
import editInputView from '@/views/article/editInput.vue'
import DailyDetailView from '@/views/daily/dailyDetail.vue'
import DailyView from '@/views/daily/daily.vue'
import FriendLinkView from '@/views/friendlink/friendlink.vue'
import FeedbackView from '@/views/feedback/feedback.vue'
import AboutView from '@/views/about/about.vue'
import SettingView from '@/views/setting/setting.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    // 保留浏览器前进/后退的滚动位置
    if (savedPosition) return savedPosition
    else return { top: 0 }
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
          ]
        },
        { path: 'friendlink', name: 'friendlink', component: FriendLinkView },
        { path: 'feedback', name: 'feedback', component: FeedbackView },
        { path: 'about', name: 'about', component: AboutView },
        { path: 'setting', name: 'setting', component: SettingView },
        { path: 'article/:id', name: 'articleDetail', component: ArticleDetailView },
        { path: 'daily/:id', name: 'dailyDetail', component: DailyDetailView },
        { path: 'editInput', name: 'editInput', component: editInputView },
      ]
    },
    { path: '/login', name: 'login', component: LoginView },
  ]
})

export default router
