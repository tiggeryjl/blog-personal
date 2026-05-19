<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/userloginstatus'
import { Edit } from '@element-plus/icons-vue'
import { queryByIdApi, updateApi } from '@/api/auth'
import { uploadApi } from '@/api/upload'

const originalUserInfo = ref({ avatar: '', username: '', nickname: '', email: '', sex: '', phone: '', createTime: '' })

// 编辑中的key数组
const editKeys = ref([])
const form = ref({ ...originalUserInfo.value })
const userStore = useUserStore()
const userId = computed(() => userStore.userInfo.id)

//获取用户信息
const getUserInfo = async () => {
  try {
    if (!userId.value) {
      ElMessage.error("未登录，请先登录！")
      return
    }
    const result = await queryByIdApi(userId.value)

    if (result && result.code === 200 && result.data) {
      originalUserInfo.value = result.data
      form.value = { ...originalUserInfo.value }

      userStore.updateUserInfo(result.data)
    } else {
      ElMessage.success("查询用户信息失败!");
    }
  } catch (error) {
    ElMessage.error("查询用户信息异常!")
  }
}

onMounted(async () => {
  const userStore = useUserStore()

  userStore.loadStorage()

  await getUserInfo()
})


// 点击修改
const handleEdit = (key) => {
  editKeys.value = editKeys.value.filter(item => form.value[item] !== originalUserInfo.value[item])
  if (!editKeys.value.includes(key)) {
    form.value[key] = originalUserInfo.value[key]
    editKeys.value.push(key)
  }
}

// 全部恢复，清空编辑
const handleCancel = () => {
  editKeys.value = []
  form.value = { ...originalUserInfo.value }
}

// 批量保存，清空编辑
const handleConfirm = async () => {
  if (editKeys.value.length === 0) return

  let updateData = {}
  for (let key of editKeys.value) {
    if (key !== 'avatar') {
      updateData[key] = form.value[key]
    }
  }

  updateData.id = userId.value

  const result = await updateApi(updateData)

  if (result && result.code == 200) {
    ElMessage.success('修改成功！')
    editKeys.value = []
    getUserInfo();
  } else {
    ElMessage.error("修改失败!");
  }
}

// 点击头像触发文件选择
const handleAvatarClick = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    try {
      const formData = new FormData()
      formData.append('file', file)

      // 第一步：把图片传给后端 → 后端上传到阿里云OSS
      const result = await uploadApi(formData)
      if (result && result.code == 200) {
        const avatarUrl = result.data

        const uid = userId.value
        const updateRes = await updateApi({
          id: uid,
          avatar: avatarUrl
        })

        if (updateRes && updateRes.code === 200) {
          // 前端实时刷新
          originalUserInfo.value.avatar = avatarUrl
          form.value.avatar = avatarUrl
          userStore.updateUserInfo({ avatar: avatarUrl })
          ElMessage.success('头像更换成功！')
        } else {
          ElMessage.error('头像更换失败')
        }
      } else {
        ElMessage.error('上传失败')
      }
    } catch (err) {
      ElMessage.error('上传异常')
    }
  }
  input.click()
}

const genderOptions = [
  { label: '男', value: 0 },
  { label: '女', value: 1 },
  { label: '保密', value: 2 }]
const formatSex = (sexVal) => {
  const sexMap = {
    0: '男',
    1: '女',
    2: '保密',
  }
  return sexMap[sexVal] || '保密'
}
</script>

<template>
  <div class="common-article-detail">
    <div class="profile-container">
      <div class="profile-header">
        <h2>个人信息</h2>
        <div class="divider"></div>
      </div>

      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="handleAvatarClick">
            <el-avatar :src="originalUserInfo.avatar" />
            <div class="avatar-hover-plus">+</div>
            <div class="avatar-tip">点击可更换头像</div>
          </div>
        </div>

        <div class="info-list">
          <div class="info-item">
            <label>昵称</label>
            <template v-if="!editKeys.includes('nickname')">
              <span>{{ originalUserInfo?.nickname }}</span>
              <el-button text size="small" icon="Edit" @click="handleEdit('nickname')">修改</el-button>
            </template>
            <input v-else v-model="form.nickname" class="custom-input" />
          </div>

          <div class="info-item">
            <label>用户名</label>
            <template v-if="!editKeys.includes('username')">
              <span>{{ originalUserInfo.username }}</span>
              <el-button text size="small" icon="Edit" @click="handleEdit('username')">修改</el-button>
            </template>
            <input v-else v-model="form.username" class="custom-input" />
          </div>

          <div class="info-item">
            <label>邮箱</label>
            <template v-if="!editKeys.includes('email')">
              <span>{{ originalUserInfo.email }}</span>
              <el-button text size="small" icon="Edit" @click="handleEdit('email')">修改</el-button>
            </template>
            <input v-else v-model="form.email" class="custom-input" />
          </div>

          <div class="info-item">
            <label>手机号</label>
            <template v-if="!editKeys.includes('phone')">
              <span>{{ originalUserInfo.phone }}</span>
              <el-button text size="small" icon="Edit" @click="handleEdit('phone')">修改</el-button>
            </template>
            <input v-else v-model="form.phone" class="custom-input" />
          </div>

          <div class="info-item">
            <label>性别</label>
            <template v-if="!editKeys.includes('sex')">
              <span>{{ formatSex(originalUserInfo?.sex) }}</span>
              <el-button text size="small" icon="Edit" @click="handleEdit('sex')">修改</el-button>
            </template>
            <div v-else class="radio-group">
              <label v-for="g in genderOptions" :key="g.value">
                <input type="radio" v-model="form.sex" :value="g.value" />
                {{ g.label }}
              </label>
            </div>
          </div>

          <div class="info-item">
            <label>创建时间</label>
            <span>{{ originalUserInfo?.createTime }}</span>
          </div>
        </div>

        <div class="action-btns">
          <button v-show="editKeys.length > 0" class="confirm-btn" @click="handleConfirm">确认</button>
          <button v-show="editKeys.length > 0" class="cancel-btn" @click="handleCancel">取消</button>
        </div>
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

.profile-card {
  border-radius: 12px;
  padding: 32px;
  width: 100%;
  height: 100%;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
  gap: 12px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 50%;
}

.avatar-wrapper:hover :deep(.el-avatar) {
  filter: brightness(0.5);
  transition: all 0.2s ease;
}

.avatar-hover-plus {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  color: white;
  font-weight: 350;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
  line-height: 1;
  margin-top: -6px;
}

.avatar-wrapper:hover .avatar-hover-plus {
  opacity: 1;
}

:deep(.el-avatar) {
  width: 100px !important;
  height: 100px !important;
  object-fit: cover !important;
  border-radius: 50% !important;
}

.avatar-tip {
  position: absolute;
  left: 50%;
  bottom: -30px;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 13px;
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  padding: 3px 8px;
  border-radius: 4px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.avatar-wrapper:hover .avatar-tip {
  opacity: 1;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
}

.info-item label {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  width: 100px;
  flex-shrink: 0;
}

.info-item span {
  font-size: 16px;
  color: var(--text-color);
  flex: unset;
  line-height: 38px;
}

.custom-input {
  height: 38px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  box-sizing: border-box;
}

.radio-group {
  display: flex;
}

.radio-group label {
  width: 60px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
  min-height: 38px;
}

.confirm-btn,
.cancel-btn {
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

.confirm-btn {
  background: var(--primary-color);
  color: var(--text-color);
}

.confirm-btn:hover {
  background-color: #ebb1a2;
  color: var(--hover-color);
}

.cancel-btn:hover {
  background-color: #c3c3c3;
  color: #000;
}
</style>