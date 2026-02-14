<template>
  <div class="profile-edit-page bg-gray-100 min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm">
      <div class="relative flex items-center justify-center h-14 px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">编辑资料</h1>
      </div>
    </header>

    <!-- Profile Form -->
    <div class="p-4 space-y-4">
      <el-card>
        <div @click="triggerAvatarUpload" class="flex items-center justify-between py-2 cursor-pointer">
          <span>头像</span>
          <div class="flex items-center">
            <el-avatar :size="48" :src="form.avatarUrl || `https://picsum.photos/150/150?random=${userStore.profile.id}`" />
            <el-icon class="ml-2 text-gray-400"><ArrowRightBold /></el-icon>
          </div>
          <input type="file" ref="avatarInput" @change="handleAvatarChange" accept="image/*" class="hidden" />
        </div>
        <el-divider class="my-1" />
        <div @click="openEditDialog('nickname')" class="flex items-center justify-between py-3 cursor-pointer">
          <span>昵称</span>
          <div class="flex items-center">
            <span class="text-gray-500">{{ form.nickname }}</span>
            <el-icon class="ml-2 text-gray-400"><ArrowRightBold /></el-icon>
          </div>
        </div>
        <el-divider class="my-1" />
        <div @click="openEditDialog('bio')" class="flex items-center justify-between py-3 cursor-pointer">
          <span>个人简介</span>
          <div class="flex items-center">
            <span class="text-gray-500 truncate max-w-xs">{{ form.bio }}</span>
            <el-icon class="ml-2 text-gray-400"><ArrowRightBold /></el-icon>
          </div>
        </div>
        <el-divider class="my-1" />
        <div @click="genderDialogVisible = true" class="flex items-center justify-between py-3 cursor-pointer">
          <span>性别</span>
          <div class="flex items-center">
            <span class="text-gray-500">{{ form.gender }}</span>
            <el-icon class="ml-2 text-gray-400"><ArrowRightBold /></el-icon>
          </div>
        </div>
        <el-divider class="my-1" />
        <div class="flex items-center justify-between py-3">
          <span>校园认证</span>
          <div class="flex items-center">
            <span v-if="userStore.profile.isCertified" class="text-green-500 flex items-center">
                <el-icon class="mr-1"><SuccessFilled /></el-icon> 已认证
            </span>
            <el-button v-else @click="certificationDialogVisible = true" type="primary" plain size="small">去认证</el-button>
          </div>
        </div>
      </el-card>

      <el-card>
         <div @click="passwordDialogVisible = true" class="flex items-center justify-between py-3 cursor-pointer">
          <span>账户与安全</span>
          <div class="flex items-center">
            <span class="text-gray-500">修改密码</span>
            <el-icon class="ml-2 text-gray-400"><ArrowRightBold /></el-icon>
          </div>
        </div>
      </el-card>

      <div class="mt-6">
        <el-button type="danger" plain class="w-full" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- Dialogs -->
    <el-dialog v-model="editDialogVisible" :title="`修改${editField.label}`" width="90%">
      <el-input v-if="editField.type === 'text'" v-model="editField.value" />
      <el-input v-if="editField.type === 'textarea'" v-model="editField.value" type="textarea" :rows="4" />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="genderDialogVisible" title="选择性别" width="90%">
        <el-radio-group v-model="form.gender" @change="handleGenderChange">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
        </el-radio-group>
        <template #footer>
            <el-button @click="genderDialogVisible = false">取消</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="90%">
        <el-form :model="passwordForm" label-position="top">
            <el-form-item label="旧密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="passwordDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="changePassword">确认修改</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="certificationDialogVisible" title="校园认证" width="90%">
        <el-form :model="certificationForm" label-position="top">
            <el-form-item label="学号/教工号">
                <el-input v-model="certificationForm.studentId" placeholder="请输入学号/教工号" />
            </el-form-item>
            <el-form-item label="真实姓名">
                <el-input v-model="certificationForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="certificationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitCertification">立即认证</el-button>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { useAuthStore } from '../../stores/auth.store';
import apiClient from '../../services/api';
import { ArrowLeftBold, ArrowRightBold, Share, MoreFilled, SuccessFilled } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();

const avatarInput = ref(null);

const form = reactive({
  nickname: '',
  bio: '',
  gender: '',
  avatarUrl: '',
});

const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
});

const certificationForm = reactive({
    studentId: '',
    realName: '',
});

const editDialogVisible = ref(false);
const genderDialogVisible = ref(false);
const passwordDialogVisible = ref(false);
const certificationDialogVisible = ref(false);

const editField = reactive({
    key: '',
    label: '',
    value: '',
    type: 'text',
});

onMounted(async () => {
    try {
        const data = await userStore.fetchProfile();
        form.nickname = data.nickname || '';
        form.bio = data.bio || '';
        form.gender = data.gender || '保密';
        form.avatarUrl = data.avatar || '';
    } catch (error) {
        console.error('获取个人资料失败:', error);
    }
});

const triggerAvatarUpload = () => {
    avatarInput.value.click();
}

const handleAvatarChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 1. 限制文件大小和类型
    if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片文件');
        return;
    }
    if (file.size > 2 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过 2MB');
        return;
    }

    // 2. 准备上传数据
    const formData = new FormData();
    formData.append('file', file);

    try {
        ElMessage.info('头像上传中...');
        // 3. 调用真实上传接口
        const response = await apiClient.post('/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        const newAvatarUrl = response.data.url;
        
        // 4. 立即更新到数据库
        await userStore.updateUserProfile({ 
            nickname: form.nickname,
            bio: form.bio,
            gender: form.gender,
            avatar: newAvatarUrl
        });
        
        form.avatarUrl = newAvatarUrl;
        ElMessage.success('头像更换成功');
    } catch (error) {
        console.error('头像上传失败:', error);
        ElMessage.error('上传失败，请稍后再试');
    }
}

const openEditDialog = (field) => {
    if (field === 'nickname') {
        editField.key = 'nickname';
        editField.label = '昵称';
        editField.value = form.nickname;
        editField.type = 'text';
    } else if (field === 'bio') {
        editField.key = 'bio';
        editField.label = '个人简介';
        editField.value = form.bio;
        editField.type = 'textarea';
    }
    editDialogVisible.value = true;
}

const saveEdit = async () => {
    const oldValue = form[editField.key];
    form[editField.key] = editField.value;
    editDialogVisible.value = false;
    
    try {
        await userStore.updateUserProfile({ 
            nickname: form.nickname,
            bio: form.bio,
            gender: form.gender,
            avatar: form.avatarUrl,
        });
        ElMessage.success(`${editField.label}修改成功`);
    } catch (error) {
        form[editField.key] = oldValue; // 失败回滚
        ElMessage.error('修改失败，请重试');
    }
}

const handleGenderChange = async (val) => {
    const oldGender = form.gender;
    form.gender = val;
    genderDialogVisible.value = false;
    try {
        await userStore.updateUserProfile({ 
            nickname: form.nickname,
            bio: form.bio,
            gender: form.gender,
            avatar: form.avatarUrl,
        });
        ElMessage.success('性别修改成功');
    } catch (error) {
        form.gender = oldGender; // 失败回滚
        ElMessage.error('修改失败');
    }
}

const changePassword = async () => {
    if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
        ElMessage.warning('请填写所有密码字段');
        return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.error('两次输入的新密码不一致');
        return;
    }
    try {
        await userStore.changePassword(passwordForm.oldPassword, passwordForm.newPassword);
        ElMessage.success('密码修改成功，请牢记新密码');
        passwordDialogVisible.value = false;
        // 清空表单
        passwordForm.oldPassword = '';
        passwordForm.newPassword = '';
        passwordForm.confirmPassword = '';
    } catch (error) {
        ElMessage.error(error.response?.data || '修改失败，请检查旧密码是否正确');
    }
}

const submitCertification = async () => {
    if (!certificationForm.studentId || !certificationForm.realName) {
        ElMessage.warning('请填写完整的认证信息');
        return;
    }
    try {
        await userStore.certifyCampus(certificationForm);
        ElMessage.success('认证成功');
        certificationDialogVisible.value = false;
    } catch (error) {
        ElMessage.error('认证失败，请稍后再试');
    }
}

const handleLogout = () => {
    ElMessageBox.confirm('您确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
        authStore.logout();
        router.push('/');
        ElMessage.success('已退出登录');
    }).catch(() => {});
}

</script>

<style scoped>
.el-divider--horizontal {
    margin: 0;
}
.truncate {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.max-w-xs {
    max-width: 10rem; /* Adjust as needed */
}
</style>
