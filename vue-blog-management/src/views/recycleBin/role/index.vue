<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getLogicDeleteRoleApi, recoverRoleApi, deleteRoleCompletelyApi } from '@/api/system/role';

const loading = ref(false);
const tableData = ref([]);

// 获取逻辑删除的角色列表
const getRoleList = async () => {
  loading.value = true;
  try {
    const result = await getLogicDeleteRoleApi();
    if (result.code == 200) {
      tableData.value = result.data || [];
    } else {
      ElMessage.error(result.msg || '角色列表加载失败');
    }
  } catch (err) {
    ElMessage.error('角色列表加载失败');
  } finally {
    loading.value = false;
  }
};

// 恢复角色
const handleRecover = (row) => {
  ElMessageBox.confirm('确认恢复该角色？恢复后角色将重新可用。', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const result = await recoverRoleApi(row.id);
      if (result.code == 200) {
        ElMessage.success('恢复成功');
        getRoleList();
      } else {
        ElMessage.error(result.msg || '恢复失败');
      }
    })
    .catch(() => {});
};

// 彻底删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm('确认彻底删除该角色？删除后数据不可恢复！', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'error',
  })
    .then(async () => {
      const result = await deleteRoleCompletelyApi(row.id);
      if (result.code == 200) {
        ElMessage.success('删除成功');
        getRoleList();
      } else {
        ElMessage.error(result.msg || '删除失败');
      }
    })
    .catch(() => {});
};

onMounted(() => {
  getRoleList();
});
</script>

<template>
  <div class="role-recycle-container">
    <h1 style="margin: 10px">角色回收站</h1>
    <el-card shadow="never">
      <el-alert type="info" :closable="false" style="margin-bottom: 15px" />

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="角色编号" prop="id" width="90" align="center" />
        <el-table-column label="角色名称" prop="roleName" align="center" />
        <el-table-column label="角色标识" prop="roleKey" align="center" />
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" align="center" />
        <el-table-column label="修改时间" prop="updateTime" align="center" />
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <span
              v-if="row.roleKey === 'admin'"
              style="color: #606266; background: #f5f7fa; padding: 4px 8px; border-radius: 4px; font-size: 13px"
              >系统超级管理员，禁止操作</span
            >
            <template v-else>
              <el-button type="success" link v-perm="'sys:recycleRole:recycle'" @click="handleRecover(row)"
                >恢复</el-button
              >
              <el-button type="danger" link v-perm="'sys:recycleRole:delete'" @click="handleDelete(row)"
                >彻底删除</el-button
              >
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped></style>
