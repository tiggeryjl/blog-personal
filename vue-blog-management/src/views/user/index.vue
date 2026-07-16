<script setup>
import { ref, watch, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MyPagination from '@/components/MyPagination.vue'
import { Plus, Search, Refresh, Edit, Delete, Lock, Unlock, UserFilled } from '@element-plus/icons-vue'
import { getUserListApi, addUserApi, updateUserStatusApi, deleteUserApi, logicDeleteUserApi, updateUserApi, getRoleAllApi, changeRoleApi } from '@/api/admin.js'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/userloginstatus'
import { storeToRefs } from 'pinia'

// 角色标识常量，只保留超管key用于判断
const ROLE_KEY_SUPER = 'admin'

const userStr = localStorage.getItem('user')
const loginUser = userStr ? JSON.parse(userStr) : null
const loginUserId = ref(loginUser?.userInfo.id ?? null)

const permissionStore = usePermissionStore()
const { permissionList: permissions } = storeToRefs(permissionStore)

//单独为上传图片功能设置请求头token，从pinia里拿
const userStore = useUserStore()

// 获取用户列表信息
const userList = ref([])

//分页操作
const total = ref(0) //总记录数
const currentPage = ref(1) //页码
const pageSize = ref(10) //每页展示的记录数

// 搜索表单
const queryForm = reactive({
  nickname: '',
  search: '',
  role: null,
  status: '',
  createTime: [],
  begin: '',
  end: ''
})

//获取用户信息
const getUserList = async () => {
  const params = {
    ...queryForm,
    page: currentPage.value,
    pageSize: pageSize.value
  };
  delete params.createTime;

  try {
    const result = await getUserListApi(params)

    if (result.code == 200) {
      userList.value = result.data.rows;
      userList.value = userList.value.map(item => ({
        ...item,
        status: item.status === 1
      }))

      total.value = result.data.total;

    } else {
      ElMessage.error(result.msg || '获取用户列表失败');
      userList.value = [];
      total.value = 0;
    }
  } catch (error) {
    ElMessage.error('网络请求失败，请稍后重试');
  }
}

// 性别
const getGenderText = (sex) => {
  if (sex === 1) return '男';
  if (sex === 0) return '女';
  return '保密';
}

/**
 * 获取用户所有角色文本拼接展示
 */
const getRoleText = (row) => {
  if (!row || !row.roleList || row.roleList.length === 0) return '普通用户'
  return row.roleList.map(item => item.roleName).join('\n')
}

/**
 * 角色标签颜色
 */
const getRoleType = (row) => {
  //超管
  if (isSuperUser(row)) return 'danger'
  // 普通管理员
  const keyList = row.roleList.map(r => r.roleKey)
  if (!keyList || keyList.length !== 0) {
    return 'primary'
  }
  // 普通用户
  return 'info'
}
/**
 * 判断用户是否拥有超级管理员角色
 */
const isSuperUser = (row) => {
  if (!row || !row.roleList || row.roleList.length === 0) return false
  return row.roleList.some(role => role.roleKey === ROLE_KEY_SUPER)
}

// 状态切换
const changeStatus = async (row) => {
  const oldStatus = !row.status;
  const newStatus = row.status ? 1 : 0;
  const tipText = newStatus ? '确定要启用该用户吗？' : '确定要禁用该用户吗？'

  await ElMessageBox.confirm(
    tipText,
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      const result = await updateUserStatusApi(row.id, newStatus)
      if (result.code == 200) {
        ElMessage.success(newStatus ? '已启用' : '已禁用')
      } else {
        ElMessage.error(result.msg);
        row.status = oldStatus;
      }
    })
    .catch(() => {
      row.status = oldStatus;
    })
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
// 表单数据
const userForm = reactive({
  id: '',
  avatar: '',
  nickname: '',
  username: '',
  phone: '',
  email: '',
  sex: '',
  intro: '',
  website: '',
  github: ''
})
// 表单验证
const userFormRef = ref(null)
const rules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/, message: '邮箱格式不正确', trigger: 'blur' }
  ]
})
// 打开新增
const openAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: '',
    nickname: '',
    username: '',
    phone: '',
    email: '',
    sex: '',
    intro: '',
    website: '',
    github: '',
    avatar: ''
  })
  nextTick(() => {
    userFormRef.value?.clearValidate()
  })
  dialogVisible.value = true
}
// 打开编辑
const openEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, row)
  nextTick(() => {
    userFormRef.value?.clearValidate()
  })
  dialogVisible.value = true
}
// 提交保存
const saveUser = async () => {
  if (!userFormRef.value) return
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let result;
        if (userForm.id) {
          result = await updateUserApi(userForm);
        } else {
          result = await addUserApi(userForm);
        }

        if (result.code == 200) {
          ElMessage.success('保存成功');
          dialogVisible.value = false
          getUserList()
        } else {
          ElMessage.error(result.msg);
        }
      } catch (error) {
        ElMessage.error('接口异常');
      }
    } else {
      ElMessage.error('表单校验不通过');
    }

  })
}

const closeDialog = () => {
  dialogVisible.value = false
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 图片上传成功后触发
const handleAvatarSuccess = (response) => {
  userForm.avatar = response.data;
}
// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}

// 权限修改
const roleDialogVisible = ref(false)
const currentRow = ref(null)
// 打开角色设置弹窗
const openRoleDialog = (row) => {
  currentRow.value = { ...row }
  // 提取当前用户已勾选的角色id数组
  selectedRoleIds.value = row.roleList.map(item => item.id)
  roleDialogVisible.value = true
}

// 弹窗选中的角色id集合
const selectedRoleIds = ref([])
// 查询条件中的角色下拉选项
const roleFilterOptions = ref([])
// 分配角色时的下拉选项
const roleAssignOptions = ref([])

// 页面加载时拉取全部角色
const getAllRoleList = async () => {
  try {
    const result = await getRoleAllApi()

    if (result.code == 200) {

      const realRoles = result.data
      const filterList = [...realRoles]
      filterList.push({
        id: 'noRoleItem',
        roleName: '普通用户',
        roleKey: 'no-role'
      })
      roleFilterOptions.value = filterList

      roleAssignOptions.value = realRoles.filter(role => role.roleKey !== ROLE_KEY_SUPER)
    }
  } catch (error) {
    ElMessage.error('服务器异常')
  }
}

// 保存角色
const saveRole = async () => {
  const editRow = currentRow.value
  const params = {
    id: editRow.id,
    roleIdList: selectedRoleIds.value
  }
  const result = await changeRoleApi(params)
  if (result.code === 200) {
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    getUserList()
  } else {
    ElMessage.error(result.msg)
  }
}
const closeRole = () => {
  roleDialogVisible.value = false
}

// 删除
const deleteUser = async (id) => {
  ElMessageBox.confirm(
    '您确认要删除该用户吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      const result = await logicDeleteUserApi(id);
      if (result.code == 200) {
        ElMessage.success('删除成功')
        getUserList()
      } else {
        ElMessage.error(result.msg);
      }
    })
}

// 批量删除选中的行
const selectedUsers = ref([])
// 表格选中事件
const handleSelectionChange = (val) => {
  selectedUsers.value = val.map(item => item.id)
}
const isCanSelect = (row) => {
  return row.id !== loginUserId.value && !isSuperUser(row)
}

// 批量删除
const handleBatchDelete = async () => {
  ElMessageBox.confirm(
    '您确认要删除选中的所有用户吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      if (selectedUsers.value && selectedUsers.value.length > 0) {
        const result = await logicDeleteUserApi(selectedUsers.value);
        if (result.code == 200) {
          ElMessage.success('批量删除成功');
          getUserList();
        } else {
          ElMessage.error(result.msg);
        }
      } else {
        ElMessage.error("您未选择任何记录");
      }
    })
}

// 重置
const resetQuery = () => {
  queryForm.nickname = ''
  queryForm.search = ''
  queryForm.role = null
  queryForm.status = ''
  queryForm.createTime = []
  queryForm.begin = ''
  queryForm.end = ''
  getUserList()
}

//侦听queryForm中的createTime属性
watch(() => queryForm.createTime, (newVal, oldVal) => {
  if (newVal.length == 2) {
    queryForm.begin = newVal[0];
    queryForm.end = newVal[1];
  } else {
    queryForm.begin = '';
    queryForm.end = '';
  }
})

onMounted(() => {
  getUserList()
  getAllRoleList()
})
</script>

<template>
  <div class="user-container" style="padding: 20px; max-width: 100%; overflow-x: hidden;">
    <div class="header-row"
      style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
      <h1 style="margin: 0;">用户管理</h1>
      <div style="display: flex; gap: 10px;">
        <el-button v-perm="'sys:user:add'" type="primary" icon="Plus" @click="openAdd">新增用户</el-button>
        <el-button v-perm="'sys:user:delete'" type="danger" icon="Delete" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>
    <!-- 查询 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" :inline="true" @submit.prevent="getUserList">

        <el-form-item label="昵称">
          <el-input v-model="queryForm.nickname" placeholder="请输入昵称" style="width:220px" clearable />
        </el-form-item>

        <el-form-item label="账号搜索">
          <el-input v-model="queryForm.search" placeholder="请输入用户名/手机号/邮箱" style="width:220px" clearable />
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" style="width:180px" clearable>
            <el-option v-for="item in roleFilterOptions" :key="item.id" :label="item.roleName" :value="item.roleKey" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" style="width:150px" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker v-model="queryForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getUserList">
            <el-icon>
              <Search />
            </el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon>
              <Refresh />
            </el-icon> 重置
          </el-button>
        </el-form-item>

      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card>
      <el-table :data="userList" stripe style="width:100%" :cell-style="{ borderRight: 'none' }"
        :header-cell-style="{ borderRight: 'none', textAlign: 'center' }" @selection-change="handleSelectionChange">

        <el-table-column type="selection" width="30" align="center" :selectable="isCanSelect" />

        <el-table-column label="头像" width="100" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-direction:column;align-items:center;gap:6px">
              <el-avatar :src="row.avatar" :icon="UserFilled" size="medium" />
              <span style="font-size:13px">{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="账号信息" min-width="200">
          <template #default="{ row }">
            <div style="line-height:1.6">
              <div>用户：{{ row.username }}</div>
              <div>手机：{{ row.phone }}</div>
              <div>邮箱：{{ row.email }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="性别" width="55" align="center">
          <template #default="{ row }"> {{ getGenderText(row.sex) }}</template>
        </el-table-column>

        <el-table-column label="角色" width="105" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-wrap:wrap;gap:3px;justify-content:center;">
              <template v-if="row.roleList && row.roleList.length > 0">
                <el-tag v-for="r in row.roleList" :key="r.id" :type="getRoleType(row)">
                  {{ r.roleName }}
                </el-tag>
              </template>
              <el-tag v-else type="info"> 普通用户 </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;justify-content:center;gap:6px;height:36px;width:100%;">
              <el-switch v-if="permissions.includes('sys:user:role')" v-model="row.status"
                :disabled="row.id === loginUserId || isSuperUser(row)" inline-prompt active-text="启用" inactive-text="禁用"
                style="--el-switch-on-color: #6ecc54; --el-switch-off-color: #fd4e4e" @change="changeStatus(row)" />
              <span v-else :style="{
                color: row.status ? '#6ecc54' : '#fd4e4e',
                width: '45px',
                background: row.status ? 'rgba(19, 206, 102, 0.1)' : 'rgba(255, 73, 73, 0.1)',
                padding: '3px 8px',
                borderRadius: '15px',
                fontSize: '13px'
              }">
                {{ row.status ? '启用' : '禁用' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createTime" width="150" align="center" />
        <el-table-column label="修改时间" prop="updateTime" width="150" align="center" />

        <el-table-column label="操作" width="260" align="center">
          <template #default="{ row }">
            <div style="display: flex; gap: 6px; flex-wrap: wrap; justify-content: center; align-items: center;">

              <template v-if="isSuperUser(row)">
                <template v-if="row.id === loginUserId && isSuperUser(row)">
                  <el-button link v-if="permissions.includes('sys:user:edit')" type="primary" icon="Edit"
                    @click="openEdit(row)">编辑</el-button>
                </template>
                <span v-else style="color:#999;">超级管理员不可修改</span>
              </template>

              <template v-else>
                <el-button link v-if="permissions.includes('sys:user:edit')" type="primary" icon="Edit"
                  @click="openEdit(row)">编辑</el-button>
                <template v-if="row.id !== loginUserId">
                  <el-button link v-if="permissions.includes('sys:user:role')" type="warning" icon="Edit"
                    @click="openRoleDialog(row)">角色</el-button>
                  <el-button link v-if="permissions.includes('sys:user:delete')" type="danger" icon="Delete"
                    @click="deleteUser(row.id)">删除</el-button>
                </template>

                <span v-if="
                  !permissions.includes('sys:user:edit') && (row.id === loginUserId
                    || !permissions.includes('sys:user:role') && !permissions.includes('sys:user:delete'))"
                  style="color:#999;font-size:13px">无操作权限</span>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-box">
      <MyPagination :total="total" :current-page="currentPage" :page-size="pageSize"
        @update:current-page="currentPage = $event; getUserList()"
        @update:page-size="pageSize = $event; getUserList()" />
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="750px" append-to-body>
      <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="100px" style="padding: 20px 30px">
        <!-- 1. 头像 独占一行 -->
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="/api/upload/upload" :show-file-list="false"
            :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload"
            :headers="{ Authorization: `Bearer ${userStore.token}` }">

            <template v-if="userForm.avatar">
              <div class="avatar-wrapper">
                <img :src="userForm.avatar" class="avatar" />
                <div class="avatar-hover-layer">
                  <el-icon size="28">
                    <Plus />
                  </el-icon>
                </div>
              </div>
            </template>
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>

          </el-upload>
        </el-form-item>

        <!-- 2. 昵称 + 用户名 一行两个 -->
        <div style="display: flex; gap: 20px;">
          <el-form-item label="昵称" style="flex:1">
            <el-input v-model="userForm.nickname" placeholder="请输入昵称(可选填)" clearable />
          </el-form-item>
          <el-form-item label="用户名" prop="username" style="flex:1">
            <el-input v-model="userForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
        </div>

        <!-- 3. 手机号 + 邮箱 一行两个 -->
        <div style="display: flex; gap: 20px;">
          <el-form-item label="手机号" prop="phone" style="flex:1">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-form-item label="邮箱" prop="email" style="flex:1">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
        </div>

        <!-- 4. 性别 独占一行 -->
        <el-form-item label="性别">
          <el-select v-model="userForm.sex" placeholder="请选择性别" style="width: 30%;">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
            <el-option label="保密" :value="2" />
          </el-select>
        </el-form-item>

        <!-- 5. 个人简介 独占一行 -->
        <el-form-item label="个人简介">
          <el-input v-model="userForm.intro" type="textarea" rows="4" placeholder="请输入个人简介"
            style="width:100%; resize:none" />
        </el-form-item>

        <!-- 6. 个人网站 + GitHub 一行两个 -->
        <div style="display: flex; gap: 20px;">
          <el-form-item label="个人网站" style="flex:1">
            <el-input v-model="userForm.website" placeholder="https://" clearable />
          </el-form-item>
          <el-form-item label="GitHub地址" style="flex:1">
            <el-input v-model="userForm.github" placeholder="GitHub地址" clearable />
          </el-form-item>
        </div>

      </el-form>

      <template #footer>
        <div style="text-align: right; padding-right: 30px;">
          <el-button @click="closeDialog">取 消</el-button>
          <el-button type="primary" @click="saveUser">保 存</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 角色设置弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配用户角色" width="450px">
      <div v-if="currentRow">
        <p>用户：{{ currentRow.nickname }}</p>
        <p>选择角色：</p>
        <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%;">
          <el-option v-for="item in roleAssignOptions" :key="item.id" :label="item.roleName" :value="item.id" />
        </el-select>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeRole">取消</el-button>
          <el-button type="primary" @click="saveRole">确认分配</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 头像容器 - 关键 */
.avatar-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

/* 悬浮遮罩层 默认隐藏 */
.avatar-hover-layer {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
  border-radius: 10px;
}

/* 悬浮显示 */
.avatar-wrapper:hover .avatar-hover-layer {
  opacity: 1;
}

/* 图片悬浮变暗 */
.avatar-wrapper:hover .avatar {
  filter: brightness(0.7);
}

.avatar-uploader .avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  display: block;
}

.avatar-uploader .avatar:hover {
  color: #ff0a0a;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 35px;
  font-weight: 500;
  color: #8c939d;
  width: 90px;
  height: 90px;
  text-align: center;
  border-radius: 10px;
  /* 添加灰色的虚线边框 */
  border: 1px dashed var(--el-border-color);
}

.el-icon.avatar-uploader-icon:hover {
  color: #036cff;
  background-color: #f8f8f8;
  border: 1px dashed #036cff;
}
</style>