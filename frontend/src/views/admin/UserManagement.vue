<template>
  <div class="user-management-page">
    <h1 class="text-2xl font-bold mb-4">用户管理</h1>
    <el-card>
      <el-table :data="userStore.allUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="nickname" label="昵称"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" :type="scope.row.status === '正常' ? 'danger' : 'success'" @click="() => handleToggleUserStatus(scope.row)">
              {{ scope.row.status === '正常' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const userStore = useUserStore();

const handleToggleUserStatus = (user) => {
    const actionText = user.status === '正常' ? '禁用' : '启用';
    ElMessageBox.confirm(`您确定要${actionText}用户 “${user.nickname}” 吗？`, '确认操作', { type: 'warning' })
    .then(() => {
        userStore.toggleUserStatus(user.id);
        ElMessage.success(`已${actionText}`);
    }).catch(() => {});
}
</script>
