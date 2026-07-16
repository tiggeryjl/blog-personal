<script setup>
import { ref, reactive } from 'vue'
import bgImg from '@/assets/blog.jpg'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userloginstatus'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/admin.js'

const router = useRouter()
const userStore = useUserStore()

// 登录表单
const loginForm = reactive({
  loginName: '',
  password: '',
  remember: false
})

// 加载状态
const loading = ref(false)

// 登录
const login = async () => {

  if (!valid()) return

  loading.value = true
  try {
    const result = await loginApi(loginForm)
    if (result.code == 200) {
      ElMessage.success('登录成功')
      userStore.loginSuccess(result.data)
      router.push('/index')
    } else {
      ElMessage.error(result.msg)
    }
  } catch {
    ElMessage.error('登录异常')
  }
  finally {
    loading.value = false
  }
}

const errors = ref({
  username: '',
  password: ''
})

const valid = () => {
  let flag = true
  errors.value.username = ''
  errors.value.password = ''

  if (!loginForm.loginName.trim()) {
    errors.value.username = '请输入账号'
    flag = false
  }
  if (!loginForm.password.trim()) {
    errors.value.password = '请输入密码'
    flag = false
  }
  return flag
}
</script>

<template>
  <div class="login-page" :style="{ backgroundImage: `url(${bgImg})` }">
    <div class="login-container">
      <div class="login-box">
        <h1>博客后台管理系统</h1>
        <p class="desc">请输入管理员账号登录</p>

        <!-- 登录表单 -->
        <div class="form">
          <div class="form-item">
            <input v-model="loginForm.loginName" type="text" placeholder="请输入账号" class="input-item" />
            <div class="error-tip">{{ errors.username }}</div>
          </div>

          <div class="form-item">
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" class="input-item" />
            <div class="error-tip">{{ errors.password }}</div>
          </div>

          <div class="remember-row">
            <label class="remember">
              <input v-model="loginForm.remember" type="checkbox" />
              记住我
            </label>
          </div>

          <button :loading="loading" class="login-btn" @click="login">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 页面整体 */
.login-page {
  width: 100vw;
  height: 100vh;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;

}

/* 登录盒子 */
.login-container {
  width: 100%;
  max-width: 420px;
  padding: 0 20px;
}

.login-box {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-box h1 {
  text-align: center;
  font-size: 26px;
  color: #333;
  margin: 0 0 8px 0;
}

.desc {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-bottom: 36px;
}

/* 表单 */
.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.error-tip {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 4px;
  height: 16px;
  line-height: 16px;
}

.form-item {
  width: 100%;
}

.input-item {
  width: 90%;
  height: 50px;
  border-radius: 10px;
  border: 1px solid #e5e6eb;
  padding: 0 16px;
  font-size: 15px;
  outline: none;
  transition: all 0.2s;
}

.input-item:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
}

/* 记住我 */
.remember-row {
  display: flex;
  align-items: center;
}

.remember {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 登录按钮 */
.login-btn {
  height: 50px;
  border-radius: 10px;
  background: #409eff;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.login-btn:hover {
  background: #338eef;
}

.login-btn:active {
  transform: scale(0.98);
}
</style>