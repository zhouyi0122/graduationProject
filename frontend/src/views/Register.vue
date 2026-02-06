<template>
  <div class="register-page bg-white min-h-screen flex flex-col">
    <!-- Top Header -->
    <header class="sticky top-0 z-40 bg-white flex-shrink-0">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
      </div>
    </header>

    <main class="flex-grow flex flex-col justify-center">
        <div class="p-8">
            <h2 class="text-3xl font-bold text-gray-800 mb-2">创建您的账户</h2>
            <p class="text-gray-500 mb-8">加入校园闲置，开始您的交易之旅</p>

            <el-form :model="form" label-position="top" class="space-y-6">
                <el-form-item label="用户名">
                <el-input v-model="form.username" placeholder="请输入用户名" size="large"></el-input>
                </el-form-item>

                <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" size="large"></el-input>
                </el-form-item>

                <el-form-item label="密码">
                <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large"></el-input>
                </el-form-item>

                <el-form-item class="pt-4">
                <el-button type="primary" @click="handleRegister" class="w-full" size="large" :loading="loading">注册</el-button>
                </el-form-item>
            </el-form>
        </div>
    </main>

    <footer class="p-8 text-center text-sm text-gray-500 flex-shrink-0">
        已有账户？ <router-link to="/login" class="font-semibold text-orange-500 hover:underline">立即登录</router-link>
    </footer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import { ElMessage } from 'element-plus';
import { ArrowLeftBold } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

const form = reactive({
  username: '',
  email: '',
  password: '',
});

const handleRegister = () => {
  if (!form.username || !form.email || !form.password) {
    ElMessage.warning('请填写所有字段');
    return;
  }
  // Basic email validation
  if (!/\S+@\S+\.\S+/.test(form.email)) {
      ElMessage.warning('请输入有效的邮箱地址');
      return;
  }

  loading.value = true;
  authStore.register(form)
    .then(() => {
      ElMessage.success('注册成功！');
      router.push('/login');
    })
    .catch(error => {
      console.error('注册失败:', error);
      const errMsg = error.response?.data?.message || '注册失败，请稍后再试。';
      ElMessage.error(errMsg);
    })
    .finally(() => {
      loading.value = false;
    });
};
</script>
