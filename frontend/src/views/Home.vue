<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold">欢迎, {{ authStore.user?.username }}!</h1>
    <p class="mt-4">这里是校园二手交易平台的首页。</p>
    <div class="space-x-4">
      <el-button @click="goToCreateProduct" type="primary" class="mt-6">发布闲置</el-button>
      <el-button @click="handleLogout" type="danger" class="mt-6">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth.store';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const router = useRouter();

const goToCreateProduct = () => {
  router.push('/products/new');
};

const handleLogout = () => {
  authStore.logout();
  ElMessage.success('您已成功退出登录。');
  router.push('/login');
};
</script>

