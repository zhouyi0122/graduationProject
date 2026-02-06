<template>
  <div class="profile-edit-page bg-gray-100 min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm">
      <div class="relative flex items-center justify-center h-14 px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">编辑资料</h1>
        <div class="absolute right-4">
            <el-button type="primary" text @click="saveProfile">保存</el-button>
        </div>
      </div>
    </header>

    <!-- Profile Form -->
    <div class="p-4 space-y-4">
      <el-card>
        <div @click="triggerAvatarUpload" class="flex items-center justify-between py-2 cursor-pointer">
          <span>头像</span>
          <div class="flex items-center">
            <el-avatar :size="48" :src="form.avatarUrl || 'https://i.pravatar.cc/150?u=a042581f4e29026704d'" />
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
        <el-radio-group v-model="form.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
            <el-radio label="保密">保密</el-radio>
        </el-radio-group>
        <template #footer>
            <el-button @click="genderDialogVisible = false">完成</el-button>
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

onMounted(() => {
    form.nickname = userStore.profile.nickname;
    form.bio = userStore.profile.bio;
    form.gender = userStore.profile.gender;
    form.avatarUrl = userStore.profile.avatarUrl;
});

const triggerAvatarUpload = () => {
    avatarInput.value.click();
}

const handleAvatarChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            form.avatarUrl = e.target.result;
        };
        reader.readAsDataURL(file);
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

const saveEdit = () => {
    form[editField.key] = editField.value;
    editDialogVisible.value = false;
}

const saveProfile = () => {
    userStore.updateProfile({ 
        nickname: form.nickname,
        bio: form.bio,
        gender: form.gender,
        avatarUrl: form.avatarUrl,
    });
    ElMessage.success('资料已保存');
    router.back();
}

const changePassword = () => {
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.error('两次输入的新密码不一致');
        return;
    }
    // Here you would typically call an API to change the password
    console.log('【模拟修改密码】', passwordForm);
    ElMessage.success('密码修改成功');
    passwordDialogVisible.value = false;
}

const submitCertification = () => {
    if (!certificationForm.studentId || !certificationForm.realName) {
        ElMessage.warning('请填写完整的认证信息');
        return;
    }
    userStore.certifyCampus(certificationForm);
    ElMessage.success('认证成功');
    certificationDialogVisible.value = false;
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
