<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePermissionStore } from '@/stores/permission'
import { storeToRefs } from 'pinia'
import { getRoleListApi, addRoleApi, updateRoleApi, deleteRoleApi, getRoleMenuTreeApi, assignRoleMenuApi } from '@/api/system/role'

const permissionStore = usePermissionStore()
const { permissionList: permissions } = storeToRefs(permissionStore)

const loading = ref(false)
const roleList = ref([])
const total = ref(0)

//搜索表单
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  roleName: '',
  status: ''
})

// 角色表单
const dialogVisible = ref(false)
const dialogTitle = ref('')
const roleRef = ref(null)
const roleForm = reactive({
  id: '',
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 1,
  remark: ''
})
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

// 权限树
const authVisible = ref(false)
const treeRef = ref(null)
const menuTree = ref([])
const checkedKeys = ref([])
const currentRoleId = ref(null)
const currentRoleKey = ref('')

// 获取列表
const getRoleList = async () => {
  try {
    loading.value = true
    const result = await getRoleListApi(queryParams)
    if (result.code == 200) {
      total.value = result.data.total
      roleList.value = result.data.rows
    } else {
      ElMessage.error("获取列表失败,请重试!")
    }
  } finally {
    loading.value = false
  }
}

// 重置
const resetQuery = () => {
  queryParams.roleName = ''
  getRoleList()
}

// 新增
const openAdd = () => {
  nextTick(() => {
    roleRef.value?.clearValidate()
  })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
  roleForm.id = ''
  roleForm.roleName = ''
  roleForm.roleKey = ''
  roleForm.sort = 1
  roleForm.status = 1
  roleForm.remark = ''
}

// 编辑
const openEdit = (row) => {
  nextTick(() => {
    roleRef.value?.clearValidate()
  })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
  Object.assign(roleForm, row)
}

// 提交表单
const submitForm = async () => {
  if (!roleRef.value) return
  roleRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (roleForm.id) {
        result = await updateRoleApi(roleForm)
      } else {
        result = await addRoleApi(roleForm)
      }

      if (result.code == 200) {
        ElMessage.success('保存成功');
        dialogVisible.value = false
        getRoleList()
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      ElMessage.error('表单校验不通过');
    }
  })
}

const closeDialog = () => {
  dialogVisible.value = false
  if (roleRef.value) {
    roleRef.value?.clearValidate()
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定删除该角色?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      const result = await deleteRoleApi(id);
      if (result.code == 200) {
        ElMessage.success('删除成功')
        getRoleList()
      } else {
        ElMessage.error(result.msg);
      }
    })
}

// 打开权限分配
const openAuth = async (row) => {
  currentRoleId.value = row.id
  currentRoleKey.value = row.roleKey
  authVisible.value = true
  const result = await getRoleMenuTreeApi(row.id)
  if (result.code == 200) {
    menuTree.value = result.data.menuTreeVOList
    checkedKeys.value = result.data.checkedKeys
  } else {
    ElMessage.error("权限查询失败,请重试!")
  }
}

// 保存权限
const saveAuth = async () => {
  const menuList = treeRef.value.getCheckedKeys()
  const params = {
    roleId: currentRoleId.value,
    menuIdList: menuList
  }
  const result = await assignRoleMenuApi(params)
  if (result.code == 200) {
    authVisible.value = false
    ElMessage.success('权限分配成功')
  } else {
    ElMessage.error(res.msg || '权限分配失败')
  }
}

const handleTreeCheck = (data, { checked }) => {
  // 获取当前节点所有子节点id
  const getChildIds = (node) => {
    let ids = []
    if (node.children && node.children.length) {
      node.children.forEach(child => {
        ids.push(child.id)
        ids = ids.concat(getChildIds(child))
      })
    }
    return ids
  }

  const childIds = getChildIds(data)
  if (checked) {
    // 选中父：所有子勾选
    treeRef.value.setCheckedKeys([...treeRef.value.getCheckedKeys(), ...childIds])
  } else {
    // 取消父：所有子取消
    let checkedArr = treeRef.value.getCheckedKeys().filter(id => !childIds.includes(id))
    treeRef.value.setCheckedKeys(checkedArr)
  }
}

onMounted(() => {
  getRoleList()
})
</script>

<template>
  <div class="role-container">
    <h1 style="margin: 10px;">角色管理</h1>
    <el-card shadow="never">
      <div class="search-bar">

        <el-form-item label="昵称">
          <el-input v-model="queryParams.roleName" placeholder="角色名称" clearable style="width:240px" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" style="width:150px" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="getRoleList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="openAdd" v-if="permissions.includes('sys:role:add')">新增角色</el-button>
        </el-form-item>
      </div>

      <el-table :data="roleList" border stripe v-loading="loading">
        <el-table-column label="角色编号" prop="id" width="90" align="center" />
        <el-table-column label="角色名称" prop="roleName" align="center" />
        <el-table-column label="角色标识" prop="roleKey" align="center" />
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" align="center" />
        <el-table-column label="创建时间" prop="updateTime" align="center" />
        <el-table-column label="操作" width="320" align="center">
          <template #default="scope">
            <span v-if="scope.row.roleKey === 'admin'"
              style="color:#606266;background:#f5f7fa;padding:4px 8px;border-radius:4px;font-size:13px;">系统超级管理员，禁止操作</span>
            <template v-else>
              <el-button type="primary" link @click="openEdit(scope.row)"
                v-if="permissions.includes('sys:role:edit') && scope.row.roleKey !== 'admin'">编辑</el-button>
              <el-button type="success" link @click="openAuth(scope.row)"
                v-if="permissions.includes('sys:role:edit') && scope.row.roleKey !== 'admin'">分配权限</el-button>
              <el-button type="danger" link @click="handleDelete(scope.row.id)"
                v-if="permissions.includes('sys:role:delete') && scope.row.roleKey !== 'admin'">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="getRoleList" @current-change="getRoleList" />
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="roleRef" :model="roleForm" label-width="100px" :rules="rules">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="ROLE_XXX" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="authVisible" title="角色权限分配" width="500px">
      <el-alert v-if="currentRoleKey === 'admin'" type="warning" title="超级管理员角色权限禁止修改" style="margin-bottom:10px" />
      <el-tree :data="menuTree" show-checkbox node-key="id" :default-checked-keys="checkedKeys" ref="treeRef"
        :props="{ label: 'menuName', children: 'children' }" :check-strictly="true" @check="handleTreeCheck"
        :disabled="currentRoleKey === 'admin'" />
      <template #footer>
        <el-button @click="authVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAuth" :disabled="currentRoleKey === 'admin'">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>