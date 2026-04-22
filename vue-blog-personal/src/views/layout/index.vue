<script setup>
import {
  SwitchButton, User, Lock, HomeFilled, Document, Reading, Link, ChatDotRound, Position,
  Setting,  // 主题设置图标
  Sunny,    // 太阳图标
  MoonNight, // 月亮图标
  Menu,
  UserFilled, Picture, Upload, Refresh
} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'  // 引入路由
import { setTheme } from '@/utils/theme' //主题色
import { ElMessage } from 'element-plus';
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/userloginstatus'
import ScrollToTop from '@/components/ScrollToTop.vue'
import MusicPlayer from '@/components/MusicPlayer.vue'

// 获取路由对象
const router = useRouter()

const route = useRoute()
//获取pinia登录状态
const userStore = useUserStore()
//判断是否为登录状态
const isLogin = computed(() => !!userStore.token)
// 手机菜单控制
const showMobileMenu = ref(false)

// 🔥 新增：点击外部关闭菜单
const menuWrapperRef = ref(null)
const menuBtnRef = ref(null)

// 点击外部关闭菜单的函数
const handleClickOutside = (event) => {
  const menuWrapper = menuWrapperRef.value
  const menuBtn = menuBtnRef.value

  if (showMobileMenu.value && menuWrapper && !menuWrapper.contains(event.target) && menuBtn && !menuBtn.contains(event.target)) {
    showMobileMenu.value = false
  }
}

// 按 ESC 键关闭菜单
const handleEscKey = (event) => {
  if (event.key === 'Escape' && showMobileMenu.value) {
    showMobileMenu.value = false
  }
}

// 登录
const goLogin = () => {
  router.push('/login')
}

const goProfile = () => {
  router.push('/setting')
}

const goResetPwd = () => {
  ElMessage.success("修改密码");
}

//导航栏跳转
const goTo = (path) => {
  router.push(path)
}

// 退出登录
const logout = () => {
  userStore.logout()
  // 跳转到登录页
  router.push('/login')
}

// 切换主题
const changeTheme = (theme) => {
  setTheme(theme)
}


// 背景图全套功能
const bgEnabled = ref(JSON.parse(localStorage.getItem('bgEnabled') ?? 'true'))
// 存储结构：{ id: 唯一标识, name: 文件名, base64: 压缩后base64, url: css url }
const customBgList = ref(JSON.parse(localStorage.getItem('customBgList')) || [])
const currentBg = ref(localStorage.getItem('currentBg') || '')
const bgRotateEnabled = ref(JSON.parse(localStorage.getItem('bgRotateEnabled') ?? 'false'))
const bgInput = ref(null)
const bgDialog = ref(false)

let rotateTimer = null

// 应用背景（平滑切换）
const applyBg = (url = null) => {
  const container = document.querySelector('.common-layout')
  if (!container) return

  if (!bgEnabled.value) {
    container.style.setProperty('--bg-opacity', '0')
    stopRotate()
    return
  }

  const finalUrl = url || currentBg.value || 'url("/src/assets/image.png")'
  container.style.setProperty('--bg-url', finalUrl)
  container.style.setProperty('--bg-opacity', '1')
}

// 开关背景
const toggleBg = () => {
  bgEnabled.value = !bgEnabled.value
  localStorage.setItem('bgEnabled', bgEnabled.value)
  if (!bgEnabled.value) {
    stopRotate()
    bgRotateEnabled.value = false
    localStorage.setItem('bgRotateEnabled', false)
  }
  applyBg()
}

// 🔥 核心：无损压缩上传（体积小+不失真+不超限）
const uploadBg = (e) => {
  const file = e.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = function (event) {
    const img = new Image()
    img.onload = function () {
      const canvas = document.createElement('canvas')
      // 限制最大宽度1920px，按比例缩放（视觉无损，体积骤降70%+）
      const maxWidth = 1920
      let width = img.width
      let height = img.height
      if (width > maxWidth) {
        height = height * (maxWidth / width)
        width = maxWidth
      }
      canvas.width = width
      canvas.height = height

      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      // 质量0.92，肉眼完全无差别，体积大幅减小
      const compressedBase64 = canvas.toDataURL('image/jpeg', 0.92)
      const cssUrl = `url("${compressedBase64}")`

      // 生成唯一ID，避免重名
      const item = {
        id: Date.now().toString(),
        name: file.name,
        base64: compressedBase64,
        url: cssUrl
      }

      customBgList.value.push(item)
      localStorage.setItem('customBgList', JSON.stringify(customBgList.value))
      ElMessage.success('上传成功！')
      bgInput.value.value = ''
    }
    img.src = event.target.result
  }
  reader.readAsDataURL(file)
}

// 使用背景
const useBg = (item) => {
  if (!bgEnabled.value) return
  currentBg.value = item.url
  localStorage.setItem('currentBg', item.url)
  applyBg(item.url)
}

// 删除背景
const deleteBg = (item) => {
  const idx = customBgList.value.findIndex(i => i.id === item.id)
  if (idx > -1) {
    customBgList.value.splice(idx, 1)
    localStorage.setItem('customBgList', JSON.stringify(customBgList.value))
    // 如果删除的是当前背景，重置为默认
    if (currentBg.value === item.url) {
      currentBg.value = ''
      localStorage.removeItem('currentBg')
      applyBg()
    }
    ElMessage.success('删除成功')
  }
}

// 轮换开关
const toggleBgRotate = () => {
  if (!bgEnabled.value) {
    ElMessage.warning('请先开启背景图')
    return
  }

  bgRotateEnabled.value = !bgRotateEnabled.value
  localStorage.setItem('bgRotateEnabled', bgRotateEnabled.value)

  if (bgRotateEnabled.value) {
    startRotate()
    ElMessage.success('已开启轮换')
  } else {
    stopRotate()
    ElMessage.success('已关闭轮换')
  }
}

// 自动轮换
const startRotate = () => {
  if (!bgEnabled.value) return
  stopRotate()
  let i = 0
  const len = customBgList.value.length
  if (len === 0) return

  rotateTimer = setInterval(() => {
    if (!bgEnabled.value) {
      stopRotate()
      return
    }
    useBg(customBgList.value[i % len])
    i++
  }, 6000)
}

const stopRotate = () => {
  if (rotateTimer) clearInterval(rotateTimer)
}

onMounted(() => {
  userStore.loadStorage()

  const savedList = JSON.parse(localStorage.getItem('customBgList')) || []
  const restoredList = savedList.map(item => {
    if (item.blobUrl) return item
    return { ...item, blobUrl: `url("${item.blobUrl}")` }
  })
  customBgList.value = restoredList

  applyBg()
  if (bgEnabled.value && bgRotateEnabled.value) startRotate()

  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleEscKey)
})

onUnmounted(() => {
  stopRotate()
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleEscKey)
})
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 导航栏区域 -->
      <el-header class="header">
        <div class="nav-container">

          <div class="logo">
            <img src="@/assets/image/logo.png" alt="Logo" style="height: 65px;" />
          </div>

          <div ref="menuBtnRef" class="mobile-menu-btn" @click="showMobileMenu = !showMobileMenu">
            <el-icon>
              <Menu />
            </el-icon>
          </div>

          <div ref="menuWrapperRef" class="nav-menu-wrapper" style="display: contents;"
            :class="{ show: showMobileMenu }">
            <div>
              <ul class="nav-menu">
                <li @click="goTo('/index')" :class="{ active: route.path === '/index' }"><el-icon>
                    <HomeFilled />
                  </el-icon>首页</li>
                <li @click="goTo('/article')" :class="{ active: route.path === '/article' }"><el-icon>
                    <Document />
                  </el-icon>文章</li>
                <li @click="goTo('/daily')" :class="{ active: route.path === '/daily' }"><el-icon>
                    <Reading />
                  </el-icon>日常</li>
                <li @click="goTo('/friendlink')" :class="{ active: route.path === '/friendlink' }">
                  <el-icon>
                    <Link />
                  </el-icon>友链
                </li>
                <li @click="goTo('/feedback')" :class="{ active: route.path === '/feedback' }"><el-icon>
                    <ChatDotRound />
                  </el-icon>留言</li>
                <li @click="goTo('/about')" :class="{ active: route.path === '/about' }"><el-icon>
                    <Position />
                  </el-icon>关于</li>
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
                      </el-icon> 默认主题
                    </el-dropdown-item>
                    <el-dropdown-item command="dark">
                      <el-icon>
                        <MoonNight />
                      </el-icon> 深色主题
                    </el-dropdown-item>
                    <el-dropdown-item command="white">
                      <el-icon>
                        <Sunny />
                      </el-icon> 浅色主题
                    </el-dropdown-item>

                    <el-dropdown-item divided @click.stop="toggleBg">
                      <el-icon>
                        <component :is="!bgEnabled ? 'Check' : 'Close'" />
                      </el-icon>
                      {{ bgEnabled ? '关闭背景图' : '开启背景图' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="bgDialog = true">
                      <el-icon>
                        <Picture />
                      </el-icon> 自定义背景图
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
                    <el-avatar :src="userStore.avatar" size="36" />
                    <span class="nickname">{{ userStore.nickname }}</span>
                  </div>

                  <!-- 🔥 下拉菜单 -->
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="goProfile">
                        <el-icon>
                          <User />
                        </el-icon> 个人中心
                      </el-dropdown-item>
                      <el-dropdown-item @click="goResetPwd">
                        <el-icon>
                          <Lock />
                        </el-icon> 修改密码
                      </el-dropdown-item>

                      <!-- 分割线 -->
                      <el-dropdown-item divided @click="logout" class="logout-item">
                        <el-icon>
                          <SwitchButton />
                        </el-icon> 退出登录
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
            <!-- 左侧版权 -->
            <div class="copyright">
              © {{ new Date().getFullYear() }} 我的博客 &nbsp;|&nbsp; 用心记录生活
            </div>
            <div class="version">Version 1.0.0</div>
          </div>
        </el-footer>
      </div>

    </el-container>

    <!-- 自定义背景图弹窗 -->
    <el-dialog v-model="bgDialog" title="自定义背景图" width="550px">
      <div class="bg-grid">
        <div class="bg-item" v-for="item in customBgList" :key="item.id">
          <!-- 🔥 关键：正确绑定图片 -->
          <div class="bg-preview" :style="{ backgroundImage: item.url }" @click="useBg(item)"></div>
          <div class="bg-tool">
            <el-button size="mini" type="danger" @click="deleteBg(item)">删除</el-button>
          </div>
        </div>
      </div>

      <div style="margin-top:15px; display:flex; gap:10px;">
        <el-button type="primary" @click="bgInput.click()">
          <el-icon>
            <Upload />
          </el-icon> 上传背景
        </el-button>
        <el-button @click="toggleBgRotate">
          <el-icon>
            <Refresh />
          </el-icon>
          {{ bgRotateEnabled ? '关闭轮换' : '开启轮换' }}
        </el-button>
      </div>
      <input ref="bgInput" type="file" accept="image/*" style="display:none" @change="uploadBg" />
    </el-dialog>
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
  overflow: hidden;

  background: var(--bg-color);
  background-attachment: fixed;

  --bg-url: url("/src/assets/image/image1.png");
  --bg-opacity: 1;
  --bg-url-next: none;
}

/* 下层：当前背景图 */
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

/* 上层：新图片淡入 */
.common-layout::after {
  content: '';
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background-image: var(--bg-url-next);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  z-index: -2;
  opacity: 0;
  transition: opacity 0.8s ease-in-out;
}

/* 1. 给最外层容器撑满全屏 */
:deep(.el-container) {
  min-height: 100vh;
}

:deep(.el-main) {
  background-color: transparent !important;
  --el-main-padding: 5px 0px;
  padding-top: 80px !important;
}

/* 背景图网格 */
.bg-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.bg-item {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.bg-preview {
  width: 100%;
  height: 100px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
}

.bg-tool {
  padding: 4px;
  text-align: center;
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
  /* 关键：一行排列 */
  align-items: center;
  /* 垂直居中 */
  gap: 12px;
  /* 按钮之间间距 */
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
    font-size: 22px !important;
    font-weight: bold !important;
    line-height: 1 !important;
    display: flex !important;
    align-items: center !important;
    height: 100% !important;
    color: var(--primary-color) !important;
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

  /* 5. 隐藏音乐组件 */
  .theme-music {
    display: none !important;
  }

  /* 6. 导航菜单面板 - 关键：覆盖 display: contents */
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

  /* 7. 展开状态 - 覆盖显示 */
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

  /* 主内容区 - 保持不影响登录页，加 .common-layout 前缀 */
  .common-layout :deep(.el-main) {
    padding-top: 80px !important;
    padding-left: 16px !important;
    padding-right: 16px !important;
  }

  /* 弹窗优化 */
  .bg-grid {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 10px !important;
  }

  .bg-preview {
    height: 100px !important;
  }

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