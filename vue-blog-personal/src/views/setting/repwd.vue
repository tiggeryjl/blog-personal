<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Eye, EyeOff } from 'lucide-vue-next'
import { editPwdApi } from '@/api/auth'

const form = ref({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})

//表单校验
const validateForm = () => {
  if (!form.value.oldPwd.trim()) {
    ElMessage.warning('请输入旧密码')
    return false
  }
  if (!form.value.newPwd.trim()) {
    ElMessage.warning('请输入新密码')
    return false
  }
  if (form.value.newPwd.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return false
  }
  if (!form.value.confirmPwd.trim()) {
    ElMessage.warning('请输入确认密码')
    return false
  }
  if (form.value.newPwd !== form.value.confirmPwd) {
    ElMessage.error('两次输入的新密码不一致')
    return false
  }
  if (form.value.oldPwd === form.value.newPwd) {
    ElMessage.warning('新密码不能与旧密码相同')
    return false
  }

  return true
}

// 提交修改
const submitPwd = async () => {
  const valid = validateForm()
  if (!valid) return

  try {
    const res = await editPwdApi({
      oldPassword: form.value.oldPwd,
      newPassword: form.value.newPwd,
      confirmPwd: form.value.confirmPwd
    })

    if (res.code === 200) {
      ElMessage.success('密码修改成功！')
      resetForm()
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (err) {
    ElMessage.error('接口异常，修改失败')
  }
}

// 重置
const resetForm = () => {
  form.value.oldPwd = ''
  form.value.newPwd = ''
  form.value.confirmPwd = ''
}

const showOldPwd = ref(false)
const showNewPwd = ref(false)
const showConfirmPwd = ref(false)
</script>

<template>
  <div class="common-article-detail">
    <div class="profile-container">
      <div class="profile-header">
        <h2>修改密码</h2>
        <div class="divider"></div>
      </div>

      <div class="form-item">
        <label>旧密码</label>
        <div class="pwd-box">
          <input v-model="form.oldPwd" :type="showOldPwd ? 'text' : 'password'" placeholder="请输入旧密码"
            class="custom-input" />
          <div class="eye-btn" @click="showOldPwd = !showOldPwd">
            <Eye v-if="showOldPwd" size="18" />
            <EyeOff v-else size="18" />
          </div>
        </div>
      </div>

      <div class="form-item">
        <label>新密码</label>
        <div class="pwd-box">
          <input v-model="form.newPwd" :type="showNewPwd ? 'text' : 'password'" placeholder="请输入新密码"
            class="custom-input" />
          <div class="eye-btn" @click="showNewPwd = !showNewPwd">
            <Eye v-if="showNewPwd" size="18" />
            <EyeOff v-else size="18" />
          </div>
        </div>
      </div>

      <div class="form-item">
        <label>确认密码</label>
        <div class="pwd-box">
          <input v-model="form.confirmPwd" :type="showConfirmPwd ? 'text' : 'password'" placeholder="请再次输入新密码"
            class="custom-input" />
          <div class="eye-btn" @click="showConfirmPwd = !showConfirmPwd">
            <Eye v-if="showConfirmPwd" size="18" />
            <EyeOff v-else size="18" />
          </div>
        </div>
      </div>

      <div class="btn-box">
        <button class="custom-btn primary" @click="submitPwd">确认修改</button>
        <button class="custom-btn reset" @click="resetForm">重置</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.common-article-detail {
  flex: 1;
  width: 100%;
  background-color: var(--card-bg);
  border-radius: 8px;
  min-height: calc(100vh - 65px);
  padding: 24px;
}

.profile-container {
  width: 100%;
  height: 100%;
}

.profile-header {
  margin-bottom: 24px;
  text-align: center;
}

.profile-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 8px 0;
}

.divider {
  height: 1px;
  background: var(--border-color);
}

.form-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  /* 让表单居中 */
  justify-content: center;
}

.form-item label {
  width: 100px;
  font-size: 16px;
  color: var(--text-secondary-color);
  flex-shrink: 0;
  text-align: right;
  padding-right: 10px;
}

/* 密码框容器 */
.pwd-box {
  position: relative;
  width: 450px;
}

/* 眼睛按钮 */
.eye-btn {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.eye-btn:hover {
  opacity: 1;
}

/* 🔥 输入框缩短 */
.custom-input {
  width: 450px;
  /* 固定宽度，不再撑满 */
  height: 45px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  transition: all 0.3s;
  background: #fff;
}

.custom-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(102, 110, 230, 0.1);
}

.custom-input::placeholder {
  color: #c0c4cc;
}

/* 🔥 按钮移动到表单正中间 */
.btn-box {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.custom-btn {
  width: 120px;
  height: 38px;
  padding: 0px 22px;
  border: 1px solid #dcdfe6;
  border-radius: 19px;
  background: #fff;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.custom-btn.primary {
  background: var(--primary-color);
  color: var(--text-color);
}

.custom-btn.primary:hover {
  background-color: #ebb1a2;
  color: var(--hover-color);
}

.custom-btn.reset:hover {
  background-color: #c3c3c3;
  color: #000000;
}
</style>