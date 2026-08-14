<script setup>
import { h, onMounted, onUnmounted, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useUserStore } from '@/stores/userloginstatus';
import { usePermissionStore } from '@/stores/permission';
import { editPwdApi, getRefreshTokenApi } from '@/api/admin';
import { getInitUnreadApi, markReadSingleApi } from '@/api/notice';
import { useNoticeStore } from '@/stores/notice';
import { useNoticePopup } from '@/utils/useNoticePopup';
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
} from '@element-plus/icons-vue';
import AiAssistant from '@/components/AiAssistant.vue';

//调用路由函数返回路由实例
const router = useRouter();
const userStore = useUserStore();
const permissionStore = usePermissionStore();
// 从pinia中响应式获取用户信息、动态菜单
const { userInfo } = storeToRefs(userStore);
const { dynamicRoutes } = storeToRefs(permissionStore);

//当前登录的用户信息
const loginName = ref(userInfo.value?.nickname || '');
const loginAvatar = ref(userInfo.value?.avatar || '');

// 过滤掉侧边栏需要隐藏的路由
const filterDynamicRoutes = computed(() => {
  return (dynamicRoutes.value || []).filter((route) => !route.meta?.hidden);
});

// 子路由过滤
const getFilterChildren = (children) => {
  return children?.filter((child) => !child.meta?.hidden) || [];
};

//修改密码
const update = async () => {
  dialogFormVisible.value = true;
  password.value = { oldPassword: '', newPassword: '' };
  //重置表单校验规则-提示信息
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
          //成功
          //给用户提示信息
          ElMessage.success('密码修改成功!');

          //关闭Dialog表单
          dialogFormVisible.value = false;
        } else {
          //失败
          //给用户提示信息
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
  //弹出确认框
  ElMessageBox.confirm('您确认要退出登录吗?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    //点击确认按钮
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
// const WS_URL = 'ws://localhost:8080/ws/admin/notice';
const WS_URL = import.meta.env.VITE_WS_URL;

const isTokenExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true; // 解析失败就当过期，走刷新
  }
};

const initWebSocket = async () => {
  const token = localStorage.getItem('token');
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
          // message: `共${result.data.unreadTotal}条未读通知，前往消息中心查看全部`,
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
});

onUnmounted(() => {
  if (socket) socket.close();
});
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="header">
        <span class="title-box">
          <img src="@/assets/images/logo.png" class="title-logo" alt="logo" />
          <span class="title">blog后台管理系统</span>
        </span>

        <div class="user-info">
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

          <el-dropdown trigger="hover">
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
        <!-- 左侧菜单：删除所有写死菜单，改为动态循环 -->
        <el-aside width="200px" class="aside">
          <el-menu router :default-active="$route.path">
            <template v-for="route in filterDynamicRoutes" :key="route.path">
              <!-- 没有子菜单 或者 只有1个子菜单：都渲染成普通菜单项（首页走这里） -->
              <el-menu-item
                v-if="!route.children?.length || route.children.length === 1"
                :index="route.children?.length ? route.children[0].path : route.path"
              >
                <el-icon>
                  <component :is="route.meta.icon" />
                </el-icon>
                <span>{{ route.meta.title }}</span>
              </el-menu-item>

              <!-- 多个子菜单才展示下拉 -->
              <el-sub-menu v-else :index="route.path">
                <template #title>
                  <el-icon>
                    <component :is="route.meta.icon" />
                  </el-icon>
                  <span>{{ route.meta.title }}</span>
                </template>
                <el-menu-item v-for="child in getFilterChildren(route.children)" :key="child.path" :index="child.path">
                  <el-icon>
                    <component :is="child.meta.icon" />
                  </el-icon>
                  <span>{{ child.meta.title }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-aside>

        <!-- 右侧核心区 -->
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>

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
}

/* 最外层容器占满全屏 */
.common-layout {
  width: 100vw;
  height: 100vh;
}

/* 头部样式不变 */
.header {
  background-image: linear-gradient(to right, #033796, #0040b7, #0092bb, #04bffd);
  padding: 0 20px;
  height: 60px !important;
  line-height: 60px;
}

.title-box {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.title-logo {
  height: 40px;
  padding-right: 5px;
  object-fit: contain;
}

.title {
  color: white;
  font-size: 40px;
  font-family: 楷体;
  font-weight: bolder;
}

.right_tool {
  display: none;
}

.user-info {
  float: right;
  height: 60px;
  display: flex;
  align-items: center;
}

.notice-badge {
  display: inline-flex;
  align-items: center;
  margin-right: 20px;
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
}

/* 左侧导航固定高度 + 自己滚动 */
.aside {
  width: 220px !important;
  border-right: 1px solid #ccc;
  height: calc(100vh - 60px);
  overflow-y: auto;

  position: sticky;
  top: 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 右侧内容区高度填满 + 独立滚动 */
:deep(.el-main) {
  height: calc(100vh - 60px);
  overflow-y: auto;
  padding: 20px;
}
</style>
