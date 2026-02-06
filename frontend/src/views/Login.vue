<template>
  <div class="login-page bg-white min-h-screen flex flex-col">
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
            <h2 class="text-3xl font-bold text-gray-800 mb-2">欢迎回来！</h2>
            <p class="text-gray-500 mb-8">登录您的校园闲置账户</p>

            <el-form :model="form" label-position="top" class="space-y-6">
                <el-form-item label="用户名">
                <el-input v-model="form.username" placeholder="请输入用户名" size="large"></el-input>
                </el-form-item>

                <el-form-item label="密码">
                <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large"></el-input>
                </el-form-item>

                <el-form-item class="pt-4">
                <el-button type="primary" @click="handleLogin" class="w-full" size="large" :loading="loading">登录</el-button>
                </el-form-item>
            </el-form>
        </div>
    </main>

    <footer class="p-8 text-center text-sm text-gray-500 flex-shrink-0">
        还没有账户？ <router-link to="/register" class="font-semibold text-orange-500 hover:underline">立即注册</router-link>
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
  password: '',
});

const handleLogin = () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  authStore.login(form.username, form.password)
    .then((isAdmin) => {
      ElMessage.success('登录成功！');
      if (isAdmin) {
        router.push('/admin');
      } else {
        router.push('/');
      }
    })
    .catch(error => {
      console.error('登录失败:', error);
      const errMsg = error.message || '登录失败，请检查您的凭据。';
      ElMessage.error(errMsg);
    })
    .finally(() => {
      loading.value = false;
    });
};
</script>
