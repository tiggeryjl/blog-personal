<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PermissionViewTip from "@/components/PermissionViewTip.vue";
import {
  getMenuTree,
  addMenuApi,
  updateMenuApi,
  logicDeleteMenuApi,
} from "@/api/system/menu";

const tableRef = ref(null);
const tableData = ref([]);
const menuTree = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const menuFormRef = ref(null);
const isExpandAll = ref(false);

// 表单类型
const menuForm = reactive({
  id: "",
  parentId: 0,
  menuName: "",
  menuType: 0,
  icon: "",
  path: "",
  component: "",
  perms: "",
  sort: 0,
  status: 1,
  hidden: false,
});

// 校验规则
const rules = ref({
  menuName: [
    { required: true, message: "菜单名称不能为空", trigger: "blur" },
    { min: 1, max: 50, message: "菜单名称长度1-50位", trigger: "blur" },
  ],
  perms: [
    {
      required: () => menuForm.menuType !== 0,
      message: "权限标识不能为空",
      trigger: ["blur"],
    },
    {
      pattern: /^[a-zA-Z0-9:-]+$/,
      message: "权限标识只能包含字母、数字、英文冒号",
      trigger: "blur",
    },
  ],
  path: [
    {
      required: () => menuForm.menuType !== 2,
      message: "当前菜单类型路由地址必填",
      trigger: "blur",
    },
    { pattern: /^\//, message: "路由必须以 / 开头", trigger: "blur" },
    { max: 255, message: "路由地址不能超过255个字符" },
  ],
  component: [
    {
      required: () => menuForm.menuType === 1,
      message: "菜单类型组件路径必填",
      trigger: "blur",
    },
    { max: 255, message: "组件路径不能超过255个字符" },
  ],
  icon: [
    {
      required: () => menuForm.menuType !== 2,
      message: "目录/菜单图标不能为空",
      trigger: "blur",
    },
    { max: 100, message: "图标名称长度不能超过100位" },
  ],
  sort: [{ type: "number", min: 0, max: 999, message: "排序必须在0~999之间" }],
});

// 展开/折叠
const toggleExpand = () => {
  isExpandAll.value = !isExpandAll.value;
  handleRecursive(tableData.value, isExpandAll.value);
};
const handleRecursive = (list, expand) => {
  list.forEach((row) => {
    tableRef.value.toggleRowExpansion(row, expand);
    if (Array.isArray(row.children) && row.children.length) {
      handleRecursive(row.children, expand);
    }
  });
};

// 获取菜单树形数据
const getMenuList = async () => {
  try {
    const result = await getMenuTree();
    if (result.code == 200) {
      tableData.value = result.data || [];
      menuTree.value = [
        {
          id: 0,
          menuName: "顶级目录",
          children: [],
        },
        ...JSON.parse(JSON.stringify(tableData.value)),
      ];
      // 刷新后折叠
      isExpandAll.value = false;
    }
  } catch (err) {
    ElMessage.error("菜单列表加载失败");
  }
};

// 新增顶级菜单
const openAddDialog = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 新增下级菜单
const openAddChild = (row) => {
  isEdit.value = false;
  resetForm();
  menuForm.parentId = row.id;
  dialogVisible.value = true;
};

// 编辑菜单
const openEditDialog = (row) => {
  isEdit.value = true;
  const copyRow = JSON.parse(JSON.stringify(row));
  Object.assign(menuForm, copyRow);
  dialogVisible.value = true;
};

// 重置表单并清空校验提示
const resetForm = () => {
  nextTick(() => {
    menuFormRef.value?.clearValidate();
  });
  menuForm.id = "";
  menuForm.parentId = 0;
  menuForm.menuName = "";
  menuForm.menuType = 0;
  menuForm.icon = "";
  menuForm.path = "";
  menuForm.component = "";
  menuForm.perms = "";
  menuForm.sort = 0;
  menuForm.status = 1;
  menuForm.hidden = false;
};

// 提交保存
const submitMenu = async () => {
  if (!menuFormRef.value) return;
  menuFormRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (menuForm.id) {
        result = await updateMenuApi(menuForm);
      } else {
        result = await addMenuApi(menuForm);
      }

      if (result.code == 200) {
        ElMessage.success("保存成功");
        dialogVisible.value = false;
        getMenuList();
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      ElMessage.error("表单校验不通过");
    }
  });
};

// 删除菜单
const handleDelete = (row) => {
  ElMessageBox.confirm(
    row.children?.length
      ? "当前菜单包含子菜单，删除会一并删除所有子菜单，确认删除？"
      : "确认删除该菜单？",
    "提示",
    {
      confirmButtonText: "确认",
      cancelButtonText: "取消",
      type: "warning",
    },
  )
    .then(async () => {
      const result = await logicDeleteMenuApi(row.id);
      if (result.code == 200) {
        ElMessage.success("删除成功");
        getMenuList();
      } else {
        ElMessage.error(result.msg || "删除失败");
      }
    })
    .catch(() => {});
};

watch(
  () => menuForm.menuType,
  (val) => {
    // 切换成按钮清空不需要的字段
    // if (val === 2) {
    //   menuForm.icon = ''
    //   menuForm.path = ''
    //   menuForm.component = ''
    // } else if (val === 0) {
    //   // 目录清空组件路径
    //   menuForm.component = ''
    // }
    // 切换类型清除之前的表单校验错误
    menuFormRef.value?.clearValidate();
  },
);

onMounted(() => {
  getMenuList();
});
</script>

<template>
  <h1 style="margin: 10px">菜单管理</h1>
  <PermissionViewTip :perms="['sys:menu:add','sys:menu:edit','sys:menu:delete']" />
  <el-card>
    <div class="mb-3">
      <el-form-item>
        <el-button type="primary" v-perm="'sys:menu:add'" @click="openAddDialog">新增菜单</el-button>
        <el-button @click="toggleExpand">{{
          isExpandAll ? "折叠全部" : "展开全部"
        }}</el-button>
      </el-form-item>
    </div>

    <el-table
      ref="tableRef"
      :data="tableData"
      border
      stripe
      row-key="id"
      tree-props="{ children: 'children' }"
    >
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
            {{ row.status === 1 ? "启用" : "禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" v-perm="'sys:menu:edit'" @click="openEditDialog(row)"
            >编辑</el-button
          >
          <el-button size="small" type="success" v-perm="'sys:menu:add'" @click="openAddChild(row)"
            >新增下级</el-button
          >
          <el-button size="small" type="danger" v-perm="'sys:menu:delete'" @click="handleDelete(row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 菜单弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="菜单编辑"
    width="650px"
    @close="resetForm"
  >
    <el-form
      ref="menuFormRef"
      :model="menuForm"
      label-width="110px"
      :rules="rules"
    >
      <el-form-item label="上级菜单">
        <el-tree-select
          v-model="menuForm.parentId"
          :data="menuTree"
          placeholder="顶级菜单"
          check-strictly
          node-key="id"
          :props="{ label: 'menuName', children: 'children' }"
          value-format="number"
        />
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
        <el-input
          v-model="menuForm.icon"
          placeholder="请输入ElementPlus图标名称，如Home"
        />
      </el-form-item>
      <el-form-item v-if="menuForm.menuType !== 2" label="路由地址" prop="path">
        <el-input v-model="menuForm.path" placeholder="例：/system/menu" />
      </el-form-item>
      <el-form-item
        v-if="menuForm.menuType === 1"
        label="组件路径"
        prop="component"
      >
        <el-input
          v-model="menuForm.component"
          placeholder="例：system/menu/index"
        />
      </el-form-item>
      <el-form-item label="权限标识" prop="perms">
        <el-input
          v-model="menuForm.perms"
          placeholder="按钮必填：sys:user:add"
        />
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
        <el-switch
          v-model="menuForm.hidden"
          active-text="隐藏"
          inactive-text="显示"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitMenu">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped></style>
