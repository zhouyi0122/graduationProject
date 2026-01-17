<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-bold text-gray-900">
        校园二手交易平台
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        登录您的账户
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow-md sm:rounded-lg sm:px-10">
        <el-form :model="form" label-position="top" class="space-y-6">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large"></el-input>
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large"></el-input>
          </el-form-item>

          <div class="pt-4">
            <el-button type="primary" @click="handleLogin" class="w-full" size="large" :loading="loading">登 录</el-button>
          </div>
        </el-form>

        <div class="mt-6">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">还没有账户？</span>
            </div>
          </div>

          <div class="mt-6">
            <el-button @click="goToRegister" class="w-full" size="large">立即注册</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import { ElMessage } from 'element-plus';

// 表单数据
const form = reactive({
  username: '',
  password: '',
});
const loading = ref(false);

const router = useRouter();
const authStore = useAuthStore();

// 处理登录逻辑
const handleLogin = () => {
  if (!form.username || !form.password) {
    ElMessage.error('用户名和密码不能为空！');
    return;
  }
  loading.value = true;
  authStore.login(form)
    .then(() => {
      ElMessage.success('登录成功！');
      router.push('/'); // 登录成功后跳转到首页
    })
    .catch(error => {
      const errMsg = error.response?.data?.message || '登录失败，请检查用户名和密码。';
      ElMessage.error(errMsg);
    })
    .finally(() => {
        loading.value = false;
    });
};

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register');
};
</script>
