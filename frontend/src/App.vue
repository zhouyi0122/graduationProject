<template>
  <div id="app">
    <header class="bg-white shadow-sm" v-if="authStore.isLoggedIn">
      <nav class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="text-lg font-bold text-gray-800">校园二手平台</div>
        <div>
          <span class="mr-4">欢迎, {{ authStore.user?.username }}</span>
          <el-button @click="handleLogout" type="danger" size="small">退出登录</el-button>
        </div>
      </nav>
    </header>

    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth.store';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  ElMessage.success('您已成功退出登录。');
  router.push('/login');
};
</script>

<style>
/* 我们可以在这里保留一些全局样式，或者将其移动到 style.css */
body {
  margin: 0;
}
</style>
