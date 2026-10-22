<script setup>
import {
  SwitchButton,
  User,
  HomeFilled,
  Document,
  Reading,
  Link,
  ChatDotRound,
  Position,
  Setting,
  Sunny,
  MoonNight,
  Menu,
  UserFilled,
} from '@element-plus/icons-vue';
import { useRouter, useRoute } from 'vue-router'; // 引入路由
import { setTheme } from '@/utils/theme'; //主题色
import { ElMessage } from 'element-plus';
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useUserStore } from '@/store/userloginstatus';
import { stopSessionRestore } from '@/router';
import { logoutApi } from '@/api/auth';
import ScrollToTop from '@/components/ScrollToTop.vue';
import MusicPlayer from '@/components/MusicPlayer.vue';

// 获取路由对象
const router = useRouter();

const route = useRoute();
//获取pinia登录状态
const userStore = useUserStore();
//判断是否为登录状态
const isLogin = computed(() => !!userStore.user_token);
// 手机菜单控制
const showMobileMenu = ref(false);

// 登录
const goLogin = () => {
  // 记录当前页面，登录成功后回到这里
  router.push({ path: '/login', query: { redirect: route.fullPath } });
};

const goProfile = () => {
  router.push('/SettingLayout');
};

//导航栏跳转
const goTo = (path) => {
  router.push(path);
};

// 退出登录
const logout = async () => {
  // 先清本地登录态，保证请求不再带过期的 token
  userStore.logout();
  // 本次会话内不再尝试用 cookie 静默恢复登录态（接口异常时兜底）
  stopSessionRestore();
  try {
    // 通知后端清除 HttpOnly 的 refreshToken cookie
    await logoutApi();
  } catch (err) {
    console.warn('退出登录接口异常：', err);
  }
  ElMessage.success('已退出登录');
  // 退出后回到首页继续浏览
  router.push('/index');
};

// 切换主题
const changeTheme = (theme) => {
  setTheme(theme);
};

// 背景图
const BG_IMAGES = [
  'https://t.alcy.cc/ycy',
  'https://t.alcy.cc/ysz',
  'https://t.alcy.cc/fj',
  'https://t.alcy.cc/pc',
  'https://t.mwm.moe/fj',
];
const applyRandomBg = () => {
  const container = document.querySelector('.common-layout');
  if (!container) return;
  const bgUrl = BG_IMAGES[Math.floor(Math.random() * BG_IMAGES.length)];
  container.style.setProperty('--bg-url', `url("${bgUrl}")`);
  container.style.setProperty('--bg-opacity', '1');
};

onMounted(() => {
  userStore.loadStorage();
  applyRandomBg();
});

// onUnmounted(() => {
// });
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 导航栏区域 -->
      <el-header class="header">
        <div class="nav-container">
          <div class="logo">
            <img src="@/assets/image/logo.png" alt="Logo" @click="goTo('/index')" />
          </div>

          <div ref="menuBtnRef" class="mobile-menu-btn" @click="showMobileMenu = !showMobileMenu">
            <el-icon>
              <Menu />
            </el-icon>
          </div>

          <div
            ref="menuWrapperRef"
            class="nav-menu-wrapper"
            style="display: contents"
            :class="{ show: showMobileMenu }"
          >
            <div>
              <ul class="nav-menu">
                <li @click="goTo('/index')" :class="{ active: route.path === '/index' }">
                  <el-icon> <HomeFilled /> </el-icon>首页
                </li>
                <li @click="goTo('/article')" :class="{ active: route.path === '/article' }">
                  <el-icon> <Document /> </el-icon>文章
                </li>
                <li @click="goTo('/daily')" :class="{ active: route.path === '/daily' }">
                  <el-icon> <Reading /> </el-icon>日常
                </li>
                <li @click="goTo('/friendlink')" :class="{ active: route.path === '/friendlink' }">
                  <el-icon> <Link /> </el-icon>友链
                </li>
                <li @click="goTo('/feedback')" :class="{ active: route.path === '/feedback' }">
                  <el-icon> <ChatDotRound /> </el-icon>留言
                </li>
                <li @click="goTo('/about')" :class="{ active: route.path === '/about' }">
                  <el-icon> <Position /> </el-icon>关于
                </li>
              </ul>
            </div>
            <div class="right_tool">
              <div class="theme-music">
                <!-- 音乐 -->
                <MusicPlayer />
              </div>

              <!-- 主题 -->
              <el-dropdown @command="changeTheme">
                <el-icon class="theme-icon">
                  <Setting />
                </el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="default">
                      <el-icon>
                        <Sunny />
                      </el-icon>
                      默认主题
                    </el-dropdown-item>
                    <el-dropdown-item command="dark">
                      <el-icon>
                        <MoonNight />
                      </el-icon>
                      深色主题
                    </el-dropdown-item>
                    <el-dropdown-item command="white">
                      <el-icon>
                        <Sunny />
                      </el-icon>
                      浅色主题
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <div v-if="!isLogin" class="login-btn" @click="goLogin">
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>去登录</span>
              </div>

              <div v-else class="user-info">
                <!-- 已登录 → 头像 + 昵称 -->
                <el-dropdown trigger="hover">
                  <div class="user-dropdown-trigger">
                    <el-avatar :src="userStore.userInfo?.avatar" size="36" />
                    <span class="nickname">{{ userStore.userInfo?.nickname || '' }}</span>
                  </div>

                  <!-- 下拉菜单 -->
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goProfile">
                        <el-icon>
                          <User />
                        </el-icon>
                        个人中心
                      </el-dropdown-item>

                      <!-- 分割线 -->
                      <el-dropdown-item divided @click="logout" class="logout-item">
                        <el-icon>
                          <SwitchButton />
                        </el-icon>
                        退出登录
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </el-header>

      <!-- main主内容区 -->
      <el-main>
        <router-view />
      </el-main>

      <!-- 向上按钮 -->
      <div>
        <ScrollToTop />
      </div>

      <!-- 页尾 -->
      <div>
        <el-footer class="footer">
          <div class="footer-container">
            <div class="copyright">© {{ new Date().getFullYear() }} 我的博客 &nbsp;|&nbsp; 用心记录生活</div>
            <div class="version">Version 1.0.0</div>
          </div>
        </el-footer>
      </div>
    </el-container>
  </div>
</template>

<style scoped>
/* 全局去掉所有Element组件的聚焦选中框 + 下拉框残留边框 */
:global(.el-dropdown-link:focus),
:global(.el-dropdown-link:focus-visible),
:global(.el-icon:focus),
:global(.el-icon:focus-visible),
:global(.el-avatar:focus),
:global(.el-dropdown-item:focus),
:global(.el-dropdown-item:focus-visible) {
  outline: none !important;
  border: none !important;
  box-shadow: none !important;
  -webkit-tap-highlight-color: transparent !important;
}

.common-layout {
  min-height: 100vh;
  position: relative;
  overflow: visible;

  background: var(--bg-color);
  background-attachment: fixed;

  --bg-opacity: 1;
}

.common-layout::before {
  content: '';
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background-image: var(--bg-url);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  transition: opacity 0.9s ease-in-out !important;
  z-index: 0;
  opacity: var(--bg-opacity) !important;
  pointer-events: none;
}

:deep(.el-container) {
  min-height: 100vh;
}

:deep(.el-main) {
  background-color: transparent !important;
  --el-main-padding: 5px 0px;
  --main-side-padding: 0px;
  overflow: visible !important;
  padding-top: 80px !important;
}

.header {
  height: 70px;
  width: 82%;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border-radius: 12px;
  background-color: var(--navbar-bg);

  /* 固定顶部 */
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
}

/* 导航内容居中容器 */
.nav-container {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo img {
  height: 60px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo img:hover {
  transform: scale(1.08);
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  list-style: none;
  gap: 5px;
  margin: 0;
  padding: 0;
  height: 100%;
}

.nav-menu li {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 39px;
  padding: 0 20px;
  color: var(--primary-color);
  font-size: 18px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: var(--text-color);
}

.nav-menu li:hover {
  background-color: #ebb1a2b8;
  color: var(--hover-color);
}

/* 导航高亮样式 */
.nav-menu li.active {
  background-color: #ebc2a2b8;
}

/* 右侧工具栏 */
.right_tool {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
  /* 禁止换行 */
}

/* 音乐组件容器样式 */
.theme-music {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-bg);
  padding: 0 12px;
  height: 42px;
  border-radius: 21px;
  transition: all 0.3s ease;
  overflow: visible;
}

.theme-music:not(:has(.music-container)) {
  display: none;
}

.theme-music:hover {
  background: var(--card-bg-hover);
}

/* 主题色设置按钮 */
.theme-icon {
  color: var(--text-color);
  cursor: pointer;
  /* 鼠标小手 */
  font-size: 22px;
  transition: color 0.3s;
}

.theme-icon:hover {
  color: var(--hover-color);
  transform: scale(1.1);
}

/* 登录按钮 */
.login-btn {
  background: linear-gradient(180deg, #ff0000 0%, #ff4040 100%);
  padding: 0px 8px;
  width: 90px;
  height: 42px;
  border-radius: 21px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fffffff2;
  cursor: pointer;
  font-size: 16px;
  transition: color 0.3s;

  justify-content: center;
  outline: none;
}

.login-btn:hover {
  background: linear-gradient(180deg, #d41212 0%, #ec3e3e 100%);
  color: #ffffff;
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-color);
  outline: none !important;
}

.nickname {
  font-size: 16px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nickname:hover {
  color: var(--hover-color);
}

/* 退出登录红色 */
:global(.logout-item) {
  color: #ff2121 !important;
}

:global(.logout-item:hover) {
  background-color: #ff0000 !important;
  color: #ffffff !important;
}

/* 电脑端隐藏按钮 */
.mobile-menu-btn {
  display: none !important;
}

.footer {
  background: var(--navbar-bg);
  backdrop-filter: blur(10px);
  padding: 24px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-color);
  overflow: visible;
  display: flex;
  align-items: flex-start;
  height: auto;
}

.footer-container {
  width: 82%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  overflow: visible;
  flex: 1;
}

.copyright {
  font-size: 14px;
  opacity: 0.85;
  text-align: center;
}

.version {
  display: block !important;
  color: var(--text-color);
  opacity: 1 !important;
  visibility: visible !important;
  line-height: 0.8;
  position: relative;
  margin-top: 0;
  margin-bottom: 0;
}

/*响应式布局 */
@media (max-width: 930px) {
  /* 1. 导航栏样式 */
  .header {
    height: 60px !important;
    width: 95% !important;
    border-radius: 12px !important;
    background-color: var(--navbar-bg) !important;
    backdrop-filter: blur(10px) !important;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1) !important;
    top: 10px !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
  }

  /* 2. 导航容器 */
  .nav-container {
    display: flex !important;
    align-items: center !important;
    justify-content: space-between !important;
    height: 100% !important;
    width: 100% !important;
    padding: 0 20px !important;
  }

  /* 3. logo样式 */
  .logo {
    display: flex;
    align-items: center;
    height: 100%;
  }

  .logo img {
    height: 50px;
    cursor: pointer;
    transition: transform 0.3s ease;
  }

  .logo img:hover {
    transform: scale(1.08);
  }

  /* 4. 汉堡按钮 */
  .mobile-menu-btn {
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    width: 40px;
    height: 40px;
    color: var(--primary-color) !important;
    font-size: 24px !important;
    cursor: pointer !important;
    border-radius: 8px;
    transition: all 0.3s ease;
    margin: 0;
    line-height: 1;
  }

  .mobile-menu-btn:active {
    background-color: rgba(0, 0, 0, 0.08);
    transform: scale(0.95);
  }

  /* 5. 移动端音乐组件保留在菜单内 */
  .theme-music {
    display: flex !important;
    align-items: center !important;
    justify-content: flex-start !important;
    width: 100% !important;
    height: 48px !important;
    padding: 6px 12px !important;
    background: transparent !important;
    border-radius: 0 !important;
    box-sizing: border-box !important;
  }

  .theme-music:not(:has(.music-container)) {
    display: none !important;
  }

  /* 6. 导航菜单面板 */
  .nav-menu-wrapper {
    display: none !important;
    position: fixed !important;
    top: 70px !important;
    left: 2.5% !important;
    width: 200px !important;
    height: auto !important;
    max-height: calc(100vh - 90px) !important;
    background: var(--navbar-bg) !important;
    backdrop-filter: blur(20px) !important;
    border-radius: 16px !important;
    padding: 20px 0 !important;
    z-index: 998 !important;
    animation: slideIn 0.3s ease;
    overflow-y: auto !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
  }

  /* 7. 展开状态 */
  .nav-menu-wrapper.show {
    display: block !important;
  }

  /* 滑入动画 */
  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateX(-100%);
    }

    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  /* 8. 导航菜单 */
  .nav-menu {
    flex-direction: column !important;
    gap: 4px !important;
    margin: 0 0 16px 0 !important;
    padding: 0 !important;
    width: 100% !important;
  }

  .nav-menu li {
    width: 100% !important;
    height: 44px !important;
    justify-content: center !important;
    padding: 0 12px !important;
    margin: 0 !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    border-radius: 0 !important;
    transition: all 0.2s ease;
    text-align: center !important;
    display: flex !important;
    align-items: center !important;
    box-sizing: border-box !important;
  }

  .nav-menu li:hover {
    background-color: #f0661b !important;
    color: #fff !important;
  }

  /* 9. 右侧工具栏 */
  .right_tool {
    flex-direction: column !important;
    align-items: stretch !important;
    gap: 4px !important;
    padding: 0 !important;
    width: 100% !important;
  }

  /* 工具栏项统一样式 */
  .theme-icon,
  .login-btn {
    width: 100% !important;
    height: 44px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    gap: 8px !important;
    padding: 0 12px !important;
    margin: 0 !important;
    font-size: 14px !important;
    box-sizing: border-box !important;
    border-radius: 0 !important;
    transition: all 0.2s ease;
  }

  .theme-icon {
    background: transparent !important;
    color: var(--primary-color) !important;
    cursor: pointer !important;
  }

  .theme-icon:hover {
    background: rgba(255, 255, 255, 0.08) !important;
  }

  .login-btn {
    background: linear-gradient(135deg, #ff6b6b 0%, #ff4040 100%) !important;
    justify-content: center !important;
    gap: 8px !important;
    margin-top: 8px !important;
    border-radius: 8px !important;
  }

  /* 用户信息区域 */
  .user-info {
    width: 100% !important;
    display: flex !important;
    justify-content: center !important;
  }

  .user-dropdown-trigger {
    width: 100% !important;
    height: 44px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    gap: 8px !important;
    padding: 0 12px !important;
    background: transparent !important;
    cursor: pointer !important;
    border-radius: 0 !important;
    transition: all 0.2s ease;
  }

  .user-dropdown-trigger:hover {
    background: rgba(255, 255, 255, 0.08) !important;
  }

  .user-dropdown-trigger :deep(.el-avatar) {
    width: 32px !important;
    height: 32px !important;
  }

  .nickname {
    font-size: 14px !important;
    max-width: 100px !important;
  }

  /* 分割线 */
  .nav-menu {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    margin-bottom: 8px !important;
    padding-bottom: 8px !important;
  }

  .common-layout :deep(.el-main) {
    --main-side-padding: 16px;
    padding-top: 80px !important;
    padding-left: var(--main-side-padding) !important;
    padding-right: var(--main-side-padding) !important;
  }

  /* 移动端弹窗统一宽度 */
  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 400px !important;
    margin: 0 auto !important;
    border-radius: 16px !important;
  }

  .nav-menu-wrapper::-webkit-scrollbar {
    width: 4px;
  }

  .nav-menu-wrapper::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 4px;
  }

  .nav-menu-wrapper::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 4px;
  }

  .footer-container {
    width: 90%;
    flex-direction: column;
    text-align: center;
    gap: 12px;
    padding: 12px 0;
  }
}
</style>
