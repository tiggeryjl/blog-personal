<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { findAllApi, addApi, queryInfoApi, updateApi, updateStatusApi, deleteCategoryApi } from '@/api/category.js'
import { findAllTagApi, addTagApi, queryInfoTagApi, updateTagApi, updateTagStatusApi, deleteTagApi } from '@/api/tag.js'

const activeTab = ref('category')

// ===================== 分类模块 =====================
const categoryList = ref([])
const categoryForm = reactive({
  id: '',
  name: '',
  description: '',
  status: 1
})
const categoryDialog = ref(false)
const isEditCategory = ref(false)
const categoryFormRef = ref(null)

const getCategoryList = async () => {
  const result = await findAllApi()
  if (result.code == 200) {
    categoryList.value = result.data
  }
}

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }]
}

//保存分类
const saveCategory = async () => {
  if (!categoryFormRef.value) return
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (categoryForm.id) {//有ID修改操作
        result = await updateApi(categoryForm);
      } else {//无ID新增操作
        result = await addApi(categoryForm);
      }

      if (result.code == 200) {
        ElMessage.success('保存成功');
        categoryDialog.value = false
        getCategoryList()
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      ElMessage.error('表单校验不通过');
    }
  })

}

const openAddCate = () => {
  isEditCategory.value = false
  Object.assign(categoryForm, { id: '', name: '', description: '', status: 1 })
  //重置表单校验规则-提示信息
  if (categoryFormRef.value) {
    categoryFormRef.value?.resetFields()
  }
  categoryDialog.value = true
}

const openEditCate = async (id) => {
  //重置表单校验规则-提示信息
  if (categoryFormRef.value) {
    categoryFormRef.value?.resetFields()
  }
  const result = await queryInfoApi(id)
  if (result.code == 200) {
    isEditCategory.value = true
    categoryDialog.value = true
    //因为用的reactive，所有直接赋值会让该变量失去响应式报错
    Object.assign(categoryForm, result.data)
  } else {
    ElMessage.error('查询分类失败');
  }
}

const categoryStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1

  const result = await updateStatusApi(row.id, newStatus)
  if (result.code == 200) {
    // row.status = newStatus
    getCategoryList()
    const msg = newStatus === 1 ? '分类已启用' : '分类已禁用'
    ElMessage.success(msg)
  } else {
    ElMessage.error('状态修改失败')
  }
}

//删除单个分类
const delCate = async (id) => {
  ElMessageBox.confirm(
    '您确认要删除该分类吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {//点击确认按钮
      const result = await deleteCategoryApi(id);
      if (result.code == 200) {
        ElMessage.success('删除成功');
        getCategoryList();
      } else {
        ElMessage.error(result.msg);
      }
    })
    .catch(() => {//点击取消按钮
      ElMessage.info('已取消删除');
    })
}

//记录复选框的分类数组ids
const categoryIds = ref([]);
const handleSelectionCateChange = (val) => {
  categoryIds.value = val.map(item => item.id);
}
// 批量删除
const delCates = async () => {
  ElMessageBox.confirm(
    '您确认要删除选中的所有分类吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      if (categoryIds.value && categoryIds.value.length > 0) {
        const result = await deleteCategoryApi(categoryIds.value);
        if (result.code == 200) {
          ElMessage.success('批量删除成功');
          getCategoryList();
        } else {
          ElMessage.error(result.msg);
        }
      } else {
        ElMessage.error("您未选择任何记录");
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    })
}

// ===================== 标签模块 =====================
const tagList = ref([])
const tagForm = reactive({ id: '', name: '', status: 1 })
const tagDialog = ref(false)
const isEditTag = ref(false)
const tagFormRef = ref(null) // 标签表单ref

const getTagList = async () => {
  const result = await findAllTagApi()
  if (result.code == 200) {
    tagList.value = result.data
  }
}

// 标签表单校验规则
const tagRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

//保存标签
const saveTag = async () => {
  if (!tagFormRef.value) return
  tagFormRef.value.validate(async (valid) => {
    if (valid) {
      let result;
      if (tagForm.id) {//有ID修改操作
        result = await updateTagApi(tagForm);
      } else {//无ID新增操作
        result = await addTagApi(tagForm);
      }

      if (result.code == 200) {
        ElMessage.success('保存成功');
        tagDialog.value = false
        getTagList()
      } else {
        ElMessage.error(result.msg);
      }
    } else {
      ElMessage.error('表单校验不通过');
    }
  })

}

const openAddTag = () => {
  isEditTag.value = false
  Object.assign(tagForm, { id: '', name: '', status: 1 })
  //重置表单校验规则-提示信息
  if (tagFormRef.value) {
    tagFormRef.value?.resetFields()
  }
  tagDialog.value = true
}

const openEditTag = async (id) => {
  if (tagFormRef.value) {
    tagFormRef.value?.resetFields()
  }
  const result = await queryInfoTagApi(id)
  if (result.code == 200) {
    isEditTag.value = true
    tagDialog.value = true
    Object.assign(tagForm, result.data)
  } else {
    ElMessage.error('查询分类失败');
  }
}

const delTag = async (id) => {
  ElMessageBox.confirm(
    '您确认要删除该标签吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {//点击确认按钮
      const result = await deleteTagApi(id);
      if (result.code == 200) {
        ElMessage.success('删除成功');
        getTagList()
      } else {
        ElMessage.error(result.msg);
      }
    })
    .catch(() => {//点击取消按钮
      ElMessage.info('已取消删除');
    })
}

const tagIds = ref([]);
const handleSelectionTagChange = (val) => {
  tagIds.value = val.map(item => item.id);
  console.log(tagIds.value)
}

const delTags = async (id) => {
  ElMessageBox.confirm(
    '您确认要删除选中的所有标签吗?',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      if (tagIds.value && tagIds.value.length > 0) {
        const result = await deleteTagApi(tagIds.value);
        if (result.code == 200) {
          ElMessage.success('批量删除成功');
          getTagList()
        } else {
          ElMessage.error(result.msg);
        }
      } else {
        ElMessage.error("您未选择任何记录");
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    })
}

const tagStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1

  const result = await updateTagStatusApi(row.id, newStatus)
  if (result.code == 200) {
    getTagList()
    const msg = newStatus === 1 ? '标签已启用' : '标签已禁用'
    ElMessage.success(msg)
  } else {
    ElMessage.error('状态修改失败')
  }
}

onMounted(() => {
  getCategoryList()
  getTagList()
})
</script>

<template>
  <div class="main-container">
    <h1>分类标签管理</h1>

    <el-tabs v-model="activeTab">
      <!-- 分类 -->
      <el-tab-pane label="分类管理" name="category">
        <div class="tab-content">
          <div class="top-bar">
            <el-button type="primary" @click="openAddCate">
              <el-icon>
                <Plus />
              </el-icon> 新增分类
            </el-button>
            <el-button type="danger" @click="delCates">
              <el-icon>
                <Minus />
              </el-icon> 批量删除
            </el-button>
          </div>

          <el-table :data="categoryList" border style="width:100%" @selection-change="handleSelectionCateChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="分类名称" prop="name" min-width="150" align="center" />
            <el-table-column label="描述" prop="description" min-width="220" align="center" />
            <el-table-column label="启用状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'">{{ row.status ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="190" align="center" />
            <el-table-column label="修改时间" prop="updateTime" width="190" align="center" />
            <el-table-column label="操作" width="220" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEditCate(row.id)">编辑</el-button>
                <el-button link type="warning" @click="categoryStatus(row)">{{ row.status ? '禁用' : '启用'
                }}</el-button>
                <el-button link type="danger" @click="delCate(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 标签 -->
      <el-tab-pane label="标签管理" name="tag">
        <div class="tab-content">
          <div class="top-bar">
            <el-button type="primary" @click="openAddTag">
              <el-icon>
                <Plus />
              </el-icon> 新增标签
            </el-button>
            <el-button type="danger" @click="delTags">
              <el-icon>
                <Minus />
              </el-icon> 批量删除
            </el-button>
          </div>

          <el-table :data="tagList" border style="width:100%" @selection-change="handleSelectionTagChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="标签名称" prop="name" min-width="120" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'">{{ row.status ? '启用' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
            <el-table-column label="修改时间" prop="updateTime" width="180" align="center" />
            <el-table-column label="操作" width="220" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEditTag(row.id)">编辑</el-button>
                <el-button link type="warning" @click="tagStatus(row)">{{ row.status ? '禁用' : '启用' }}</el-button>
                <el-button link type="danger" @click="delTag(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 分类弹窗 -->
    <el-dialog v-model="categoryDialog" :title="isEditCategory ? '编辑分类' : '新增分类'" width="520px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="状态" v-if="!isEditCategory">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">确认</el-button>
      </template>
    </el-dialog>

    <!-- 标签弹窗 -->
    <el-dialog v-model="tagDialog" :title="isEditTag ? '编辑标签' : '新增标签'" width="500px">
      <el-form ref="tagFormRef" :model="tagForm" :rules="tagRules" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="状态" v-if="!isEditTag">
          <el-radio-group v-model="tagForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTag">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.main-container {
  padding: 0 20px;
}

.main-container h1 {
  padding-bottom: 20px;
}

:deep(.el-tabs__nav-wrap .el-tabs__item) {
  font-size: 18px;
  font-weight: 500;
  height: 50px;
  line-height: 50px;
}

.tab-content {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.top-bar {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>