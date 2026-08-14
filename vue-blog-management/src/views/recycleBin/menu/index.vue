<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getdeleteMenuTree, recoverMenuApi, deleteMenuApi } from '@/api/system/menu'

const tableRef = ref(null)
const tableData = ref([])
const menuTree = ref([])
const dialogVisible = ref(false)
const isExpandAll = ref(false)

// 表单类型
const menuForm = reactive({
  id: '',
  parentId: 0,
  menuName: '',
  menuType: 0,
  icon: '',
  path: '',
  component: '',
  perms: '',
  sort: 0,
  status: 1,
  hidden: false
})

// 展开/折叠
const toggleExpand = () => {
  isExpandAll.value = !isExpandAll.value
  handleRecursive(tableData.value, isExpandAll.value)
}
const handleRecursive = (list, expand) => {
  list.forEach(row => {
    tableRef.value.toggleRowExpansion(row, expand)
    if (Array.isArray(row.children) && row.children.length) {
      handleRecursive(row.children, expand)
    }
  })
}

// 获取菜单树形数据
const getMenuList = async () => {
  try {
    const result = await getdeleteMenuTree()
    if (result.code == 200) {
      tableData.value = result.data || []
      menuTree.value = [
        {
          id: 0,
          menuName: '顶级目录',
          children: []
        },
        ...JSON.parse(JSON.stringify(tableData.value))
      ]
      // 刷新后折叠
      isExpandAll.value = false
    }
  } catch (err) {
    ElMessage.error('菜单列表加载失败')
  }
}


// 查看详情
const openEditDialog = (row) => {
  const copyRow = JSON.parse(JSON.stringify(row))
  Object.assign(menuForm, copyRow)
  dialogVisible.value = true
}

// 恢复菜单
const handleRecover = (row) => {
  ElMessageBox.confirm(
    row.children?.length
      ? '当前菜单包含子菜单，恢复会一并恢复所有子菜单，确认恢复？'
      : '确认恢复该菜单？',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    const result = await recoverMenuApi(row.id)
    if (result.code == 200) {
      ElMessage.success('恢复成功')
      getMenuList()
    } else {
      ElMessage.error(result.msg || '恢复失败');
    }
  }).catch(() => { })
}

// 删除菜单
const handleDelete = (row) => {
  ElMessageBox.confirm(
    row.children?.length
      ? '当前菜单包含子菜单，删除将会一并彻底删除所有子菜单，确认删除？'
      : '确认彻底删除该菜单？',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    const result = await deleteMenuApi(row.id)
    if (result.code == 200) {
      ElMessage.success('删除成功')
      getMenuList()
    } else {
      ElMessage.error(result.msg || '删除失败');
    }
  }).catch(() => { })
}

onMounted(() => {
  getMenuList()
})
</script>

<template>
  <h1 style="margin: 10px;">权限回收站</h1>
  <el-card>
    <div class="mb-3">
      <el-form-item>
        <el-button @click="toggleExpand">{{ isExpandAll ? '折叠全部' : '展开全部' }}</el-button>
      </el-form-item>
    </div>

    <el-table ref="tableRef" :data="tableData" border stripe row-key="id" tree-props="{ children: 'children' }">
      <el-table-column label="菜单名称" prop="menuName" align="center" />
      <el-table-column label="图标" align="center">
        <template #default="{ row }">
          <template v-if="row.icon">
            <el-icon>
              <component :is="row.icon" />
            </el-icon>
            【{{ row.icon }}】
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="路由地址" prop="path" align="center" />
      <el-table-column label="组件路径" prop="component" align="center" />
      <el-table-column label="权限标识" prop="perms" align="center" />
      <el-table-column label="菜单类型" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.menuType === 0">目录</span>
          <span v-else-if="row.menuType === 1">菜单</span>
          <span v-else>按钮(B)</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="55" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">查看详情</el-button>
          <el-button size="small" type="success" v-perm="'sys:recycleMenu:recycle'" @click="handleRecover(row)">恢复</el-button>
          <el-button size="small" type="danger" v-perm="'sys:recycleMenu:delete'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 菜单弹窗 -->
  <el-dialog v-model="dialogVisible" title="菜单编辑" width="650px" @close="resetForm">
    <el-form ref="menuFormRef" :model="menuForm" label-width="110px" :rules="rules">
      <el-form-item label="上级菜单">
        <el-tree-select v-model="menuForm.parentMenuName" :data="menuTree" placeholder="顶级菜单" check-strictly
          node-key="id" :props="{ label: 'menuName', children: 'children' }" value-format="number" />
      </el-form-item>
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
      </el-form-item>
      <el-form-item label="菜单类型" prop="menuType">
        <el-radio-group v-model="menuForm.menuType">
          <el-radio :label="0">目录</el-radio>
          <el-radio :label="1">菜单</el-radio>
          <el-radio :label="2">按钮</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="menuForm.menuType !== 2" label="图标" prop="icon">
        <el-input v-model="menuForm.icon" placeholder="请输入ElementPlus图标名称，如Home" />
      </el-form-item>
      <el-form-item v-if="menuForm.menuType !== 2" label="路由地址" prop="path">
        <el-input v-model="menuForm.path" placeholder="例：/system/menu" />
      </el-form-item>
      <el-form-item v-if="menuForm.menuType === 1" label="组件路径" prop="component">
        <el-input v-model="menuForm.component" placeholder="例：system/menu/index" />
      </el-form-item>
      <el-form-item label="权限标识" prop="perms">
        <el-input v-model="menuForm.perms" placeholder="按钮必填：system:user:add" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="menuForm.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="menuForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否隐藏">
        <el-switch v-model="menuForm.hidden" active-text="隐藏" inactive-text="显示" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped></style>
