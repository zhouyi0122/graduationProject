<template>
  <el-container class="admin-layout-container h-screen">
    <el-aside width="200px" class="bg-gray-800 text-white flex flex-col">
      <div class="h-16 flex items-center justify-center text-xl font-bold flex-shrink-0">
        后台管理
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        class="el-menu-vertical-demo border-r-0"
        text-color="#fff"
        active-text-color="#ffd04b"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>消息中心</span>
        </el-menu-item>
      </el-menu>
      <div class="mt-auto p-4">
          <el-button type="danger" plain class="w-full" @click="handleLogout">退出登录</el-button>
      </div>
    </el-aside>
    <el-main class="bg-gray-100 p-6">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import { DataLine, User, Goods, Collection, List, ChatDotRound } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = ref(route.path);

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath;
});

const handleLogout = () => {
    ElMessageBox.confirm('您确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
        authStore.logout();
        router.push('/login');
        ElMessage.success('已退出登录');
    }).catch(() => {});
}
</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
.el-menu {
    border-right: none;
}
</style>
