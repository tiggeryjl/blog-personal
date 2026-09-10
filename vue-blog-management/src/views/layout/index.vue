<script setup>
import { h, onMounted, onUnmounted, computed, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useUserStore } from '@/stores/userloginstatus';
import { usePermissionStore } from '@/stores/permission';
import { editPwdApi, getRefreshTokenApi } from '@/api/admin';
import { getInitUnreadApi, markReadSingleApi, getOnlineCountApi } from '@/api/notice';
import { ONLINE_COUNT_REFRESH_INTERVAL } from '@/constants/noticeConstants';
import { useNoticeStore } from '@/stores/notice';
import { useNoticePopup } from '@/utils/useNoticePopup';
import { useMobile } from '@/utils/useResponsive';
import SidebarMenu from '@/components/SidebarMenu.vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import {
  EditPen,
  SwitchButton,
  Promotion,
  House,
  HomeFilled,
  UserFilled,
  Setting,
  HelpFilled,
  Avatar,
  PieChart,
  InfoFilled,
  Document,
  Share,
  Menu,
  Monitor,
} from '@element-plus/icons-vue';
import AiAssistant from '@/components/AiAssistant.vue';

//调用路由函数返回路由实例
const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const permissionStore = usePermissionStore();
// 从pinia中响应式获取用户信息、动态菜单
const { userInfo } = storeToRefs(userStore);
const { dynamicRoutes } = storeToRefs(permissionStore);

// 移动端布局：手机端侧边栏收起为抽屉，由头部按钮唤起
const { isMobile } = useMobile();
const drawerVisible = ref(false);
// 抽屉宽度
const MOBILE_DRAWER_SIZE = 240;

// 切换路由后自动收起移动端抽屉
watch(
  () => route.path,
  () => {
    drawerVisible.value = false;
  }
);

// 从移动端切回 PC 端时关闭抽屉
watch(isMobile, (value) => {
  if (!value) {
    drawerVisible.value = false;
  }
});

//当前登录的用户信息
const loginName = ref(userInfo.value?.nickname || '');
const loginAvatar = ref(userInfo.value?.avatar || '');

// 过滤掉侧边栏需要隐藏的路由
const filterDynamicRoutes = computed(() => {
  return (dynamicRoutes.value || []).filter((route) => !route.meta?.hidden);
});

//修改密码
const update = async () => {
  dialogFormVisible.value = true;
  password.value = { oldPassword: '', newPassword: '' };
  //重置表单校验规则
  if (updatepsw.value) {
    updatepsw.value.resetFields();
  }
};
//保存修改
const save = async () => {
  if (!updatepsw.value) return;
  updatepsw.value.validate(async (valid) => {
    if (valid) {
      //通过
      if (password.value.oldPassword != password.value.newPassword) {
        const result = await editPwdApi(password.value);

        if (result.code == 200) {
          ElMessage.success('密码修改成功!');

          dialogFormVisible.value = false;
        } else {
          ElMessage.error(result.msg);
        }
      } else {
        ElMessage.error('新密码与旧密码一致');
      }
    } else {
      //不通过
      ElMessage.error('表单校验不通过');
    }
  });
};

//退出
const loginout = () => {
  ElMessageBox.confirm('您确认要退出登录吗?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    userStore.logout();
    permissionStore.resetPermission();
    router.push('/login');
    if (socket) {
      socket.close();
      socket = null;
    }
    ElMessage.success('退出成功');
  });
};

const dialogFormVisible = ref(false);
const formLabelWidth = '80px';
const updatepsw = ref();

const password = ref({ oldPassword: '', newPassword: '' });
//表单验证规则
const rules = ref({
  oldPassword: [
    { required: true, message: '未输入内容', trigger: 'blur' },
    { min: 6, message: '密码不可少于6位', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '未输入内容', trigger: 'blur' },
    { min: 6, message: '密码不可少于6位', trigger: 'blur' },
  ],
});

const noticeStore = useNoticeStore();
const { push } = useNoticePopup();
let socket = null;
// 通知WebSocket地址，用当前访问的域名拼接
const WS_PATH = import.meta.env.VITE_WS_URL || '/ws/admin/notice';
const WS_URL = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}${WS_PATH}`;

// 在线管理端数量
const onlineCount = ref(0);
let onlineTimer = null;

const loadOnlineCount = async () => {
  try {
    const result = await getOnlineCountApi();
    if (result.code === 200) {
      onlineCount.value = Number(result.data) || 0;
    }
  } catch (error) {
    console.warn('获取在线人数失败:', error);
  }
};

const isTokenExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true; // 解析失败就当过期，走刷新
  }
};

const initWebSocket = async () => {
  let token = localStorage.getItem('token');
  if (!token) return;

  if (isTokenExpired(token)) {
    try {
      const result = await getRefreshTokenApi();
      userStore.setToken(result.data.token);
      token = result.data.token;
    } catch (e) {
      return;
    }
  }
  try {
    if ('WebSocket' in window) {
      const wsUrl = `${WS_URL}?token=${token}`;
      socket = new WebSocket(wsUrl);
    } else {
      console.error('当前浏览器不支持WebSocket');
      return;
    }
  } catch (error) {
    console.error('WebSocket连接失败:', error);
    setTimeout(initWebSocket, 3000);
    return;
  }

  socket.onopen = () => {
    console.log('WebSocket连接成功');
    loadOnlineCount();
  };

  socket.onmessage = async (event) => {
    const data = JSON.parse(event.data);
    push(data);
    // 收到通知消息，更新未读消息数量
    const result = await getInitUnreadApi();
    if (result.code === 200) {
      noticeStore.setCount(result.data.unreadTotal);
    }
  };

  socket.onclose = () => {
    console.log('WebSocket连接断开');
    if (!localStorage.getItem('token')) return;
    setTimeout(initWebSocket, 3000);
  };

  socket.onerror = () => {
    socket.close();
  };
};

const loadOfflineNotice = async () => {
  const result = await getInitUnreadApi();
  if (result.code === 200) {
    noticeStore.setCount(result.data.unreadTotal);
    if (result.data.latestList.length === 0) return;
    result.data.latestList.forEach((item) => push(item));
    if (result.data.unreadTotal > 5) {
      setTimeout(() => {
        ElNotification({
          title: '通知汇总',
          message: h('p', null, [
            h('span', { style: 'color: #606266' }, '共'),
            h('strong', { style: 'color: #0279da' }, `${result.data.unreadTotal}`),
            h('span', { style: 'color: #606266' }, '条未读通知，前往消息中心查看全部'),
          ]),
          duration: 7000,
          onClick: () => router.push('/notice'),
          type: 'warning',
          progress: {
            color: [
              { color: '#ff3e3e', percentage: 20 },
              { color: '#e6a23c', percentage: 40 },
              { color: '#5cb873', percentage: 60 },
              { color: '#02da99', percentage: 80 },
              { color: '#0279da', percentage: 100 },
            ],
          },
        });
      }, result.data.latestList.length * 750 + 500);
    }
  }
};

onMounted(async () => {
  await loadOfflineNotice();
  initWebSocket();
  loadOnlineCount();
  onlineTimer = setInterval(loadOnlineCount, ONLINE_COUNT_REFRESH_INTERVAL);
});

onUnmounted(() => {
  if (socket) socket.close();
  if (onlineTimer) {
    clearInterval(onlineTimer);
    onlineTimer = null;
  }
});
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header">
        <span class="title-box">
          <!-- 移动端菜单按钮 -->
          <el-icon v-if="isMobile" class="menu-toggle" @click="drawerVisible = true">
            <Menu />
          </el-icon>
          <img src="@/assets/images/logo.png" class="title-logo" alt="logo" />
          <span class="title">blog后台管理系统</span>
        </span>

        <div class="user-info">
          <span class="online-box" title="当前在线的管理端数量（同一账号多端登录分别计数）">
            <el-icon class="online-icon"><Monitor /></el-icon>
            <span class="online-label">在线</span>
            <span class="online-count">{{ onlineCount }}</span>
          </span>

          <el-badge
            :value="noticeStore.unreadCount"
            max="99"
            :hidden="noticeStore.unreadCount === 0"
            class="notice-badge"
          >
            <div class="bell-wrap" @click="$router.push('/notice')">
              <el-icon class="bell-icon"><Bell /></el-icon>
            </div>
          </el-badge>

          <el-dropdown :trigger="isMobile ? 'click' : 'hover'">
            <div class="user-dropdown-trigger">
              <el-avatar :src="loginAvatar" size="32" />
              <span class="username">管理员：{{ loginName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="update">
                  <el-icon>
                    <EditPen />
                  </el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided @click="loginout">
                  <el-icon>
                    <SwitchButton />
                  </el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 左侧菜单：PC 端固定侧边栏 -->
        <el-aside v-if="!isMobile" width="200px" class="aside">
          <SidebarMenu :routes="filterDynamicRoutes" />
        </el-aside>

        <!-- 右侧核心区 -->
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>

      <!-- 移动端侧边栏抽屉 -->
      <el-drawer
        v-model="drawerVisible"
        class="sidebar-drawer"
        direction="ltr"
        title="功能菜单"
        :size="MOBILE_DRAWER_SIZE"
      >
        <SidebarMenu :routes="filterDynamicRoutes" @select="drawerVisible = false" />
      </el-drawer>

      <AiAssistant />
    </el-container>
  </div>

  <!-- 修改密码对话框 -->
  <el-dialog v-model="dialogFormVisible" title="修改密码" width="500">
    <el-form :model="password" :rules="rules" ref="updatepsw">
      <el-form-item prop="oldPassword" label="旧密码" :label-width="formLabelWidth">
        <el-input v-model="password.oldPassword" autocomplete="off" type="password" placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item prop="newPassword" label="新密码" :label-width="formLabelWidth">
        <el-input v-model="password.newPassword" autocomplete="off" type="password" placeholder="请输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save"> 保存 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
:global(body) {
  margin: 0;
  padding: 0;
  overflow: hidden;
  -webkit-text-size-adjust: 100%;
}

/* 最外层容器占满全屏 */
.common-layout {
  /* 顶部栏高度，移动端会通过媒体查询调小 */
  --layout-header-height: 60px;
  width: 100%;
  max-width: 100%;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
}

.header {
  background-image: linear-gradient(to right, #033796, #0040b7, #0092bb, #04bffd);
  padding: 0 20px;
  height: var(--layout-header-height) !important;
  line-height: var(--layout-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.title-box {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.title-logo {
  height: 40px;
  padding-right: 5px;
  object-fit: contain;
  flex-shrink: 0;
}

.title {
  color: white;
  font-size: 40px;
  font-family: 楷体;
  font-weight: bolder;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-toggle {
  font-size: 22px;
  color: #ffffff;
  cursor: pointer;
  flex-shrink: 0;
}

.right_tool {
  display: none;
}

.user-info {
  height: var(--layout-header-height);
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.notice-badge {
  display: inline-flex;
  align-items: center;
  margin-right: 20px;
}

.online-box {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 12px;
  margin-right: 16px;
  border-radius: 16px;
  background-color: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 13px;
  line-height: 1;
  white-space: nowrap;
}

.online-icon {
  font-size: 15px;
}

.online-count {
  font-weight: 600;
}
/* 调整角标距离铃铛 */
.notice-badge :deep(.el-badge__content) {
  top: 10px;
  right: 10px;
}

.bell-wrap {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #ddd9d9;
}
.bell-wrap:hover {
  background-color: #0298ca;
  border-radius: 50%;
}
.bell-wrap:hover .bell-icon {
  font-size: 22px;
  color: #ffffff;
}
.bell-icon {
  font-size: 20px;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
}

.username {
  font-size: 15px;
  white-space: nowrap;
}

/* 左侧导航固定高度 + 自己滚动 */
.aside {
  width: 220px !important;
  border-right: 1px solid #ccc;
  height: calc(100vh - var(--layout-header-height));
  height: calc(100dvh - var(--layout-header-height));
  overflow-y: auto;
  flex-shrink: 0;

  position: sticky;
  top: 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 右侧内容区高度填满 + 独立滚动 */
:deep(.el-main) {
  height: calc(100vh - var(--layout-header-height));
  height: calc(100dvh - var(--layout-header-height));
  overflow-y: auto;
  padding: 20px;
}

@media (max-width: 768px) {
  .common-layout {
    --layout-header-height: 56px;
  }

  .header {
    padding: 0 12px;
    gap: 8px;
  }

  .title-box {
    gap: 8px;
  }

  .title-logo {
    height: 28px;
    padding-right: 0;
  }

  .title {
    font-size: 16px;
  }

  .username {
    display: none;
  }

  .online-box {
    height: 28px;
    padding: 0 8px;
    margin-right: 6px;
    gap: 3px;
    font-size: 12px;
  }

  .online-label {
    display: none;
  }

  .notice-badge {
    margin-right: 8px;
  }

  .notice-badge :deep(.el-badge__content) {
    top: 8px;
    right: 8px;
  }

  .bell-wrap {
    width: 34px;
    height: 34px;
  }

  :deep(.el-main) {
    padding: 10px;
  }
}
</style>
