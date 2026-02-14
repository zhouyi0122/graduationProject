<template>
  <div class="user-management-page">
    <h1 class="text-2xl font-bold mb-4">用户管理</h1>
    <el-card v-loading="loading">
      <el-table :data="userStore.allUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="nickname" label="昵称"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column label="角色">
          <template #default="scope">
            <el-tag :type="scope.row.role === 1 ? 'warning' : 'info'">
              {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <!-- 避免管理员禁用自己 -->
            <el-button 
              size="small" 
              :type="scope.row.status === 0 ? 'danger' : 'success'" 
              @click="() => handleToggleUserStatus(scope.row)"
            >
              {{ scope.row.status === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const userStore = useUserStore();
const loading = ref(false);

const loadUsers = async () => {
  loading.value = true;
  try {
    await userStore.fetchAllUsers();
  } catch (error) {
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadUsers();
});

const handleToggleUserStatus = (user) => {
    const actionText = user.status === 0 ? '禁用' : '启用';
    ElMessageBox.confirm(`您确定要${actionText}用户 “${user.nickname || user.username}” 吗？`, '确认操作', { 
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    .then(async () => {
        try {
          await userStore.adminToggleUserStatus(user.id);
          ElMessage.success(`用户已${actionText}`);
        } catch (error) {
          ElMessage.error(`${actionText}失败，请稍后再试`);
        }
    }).catch(() => {});
}
</script>
