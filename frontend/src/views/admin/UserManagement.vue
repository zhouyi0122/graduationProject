<template>
  <div class="user-management-page">
    <h1 class="text-2xl font-bold mb-4">用户管理</h1>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="用户列表" name="users">
        <el-card v-loading="loadingUsers">
          <el-table :data="userStore.allUsers" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="email" label="邮箱" />
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
      </el-tab-pane>

      <el-tab-pane label="校园认证管理" name="certifications">
        <el-card>
          <div class="mb-4 flex items-center gap-2">
            <el-select v-model="certificationStatusFilter" placeholder="按状态筛选" style="width: 180px" @change="loadCertifications">
              <el-option label="全部" value="" />
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已驳回" :value="2" />
            </el-select>
          </div>

          <el-table v-loading="loadingCertifications" :data="userStore.certificationApplications" style="width: 100%">
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="nickname" label="昵称" width="140" />
            <el-table-column prop="school" label="学校" min-width="160" />
            <el-table-column prop="studentId" label="学号/教工号" min-width="130" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column label="状态" width="110">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已驳回</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewRemark" label="审核备注" min-width="180" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="success"
                  @click="handleApprove(scope.row)"
                >
                  通过
                </el-button>
                <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="danger"
                  @click="handleReject(scope.row)"
                >
                  驳回
                </el-button>
                <span v-else class="text-gray-400 text-xs">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const userStore = useUserStore();
const activeTab = ref('users');

const loadingUsers = ref(false);
const loadingCertifications = ref(false);
const certificationStatusFilter = ref('');

const loadUsers = async () => {
  loadingUsers.value = true;
  try {
    await userStore.fetchAllUsers();
  } catch (error) {
    ElMessage.error('加载用户列表失败');
  } finally {
    loadingUsers.value = false;
  }
};

const loadCertifications = async () => {
  loadingCertifications.value = true;
  try {
    const status = certificationStatusFilter.value === '' ? undefined : certificationStatusFilter.value;
    await userStore.fetchCertificationApplications(status);
  } catch (error) {
    ElMessage.error('加载认证申请列表失败');
  } finally {
    loadingCertifications.value = false;
  }
};

onMounted(async () => {
  await Promise.all([loadUsers(), loadCertifications()]);
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
    })
    .catch(() => {});
};

const handleApprove = async (row) => {
  try {
    await userStore.approveCertification(row.id);
    ElMessage.success('审核通过成功');
    await Promise.all([loadCertifications(), loadUsers()]);
  } catch (error) {
    ElMessage.error(error.response?.data || '审核通过失败');
  }
};

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因（可选）', '驳回认证', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：学号与姓名不匹配'
    });
    await userStore.rejectCertification(row.id, value || '');
    ElMessage.success('已驳回该申请');
    await Promise.all([loadCertifications(), loadUsers()]);
  } catch (error) {
    if (error === 'cancel' || error === 'close') return;
    ElMessage.error(error.response?.data || '驳回失败');
  }
};
</script>
