<template>
  <div class="user-page-container bg-gray-100 min-h-screen">
    <!-- Top section -->
    <div class="relative pb-10">
      <!-- Background -->
      <div class="absolute top-0 left-0 right-0 h-40 bg-gradient-to-br from-orange-400 to-pink-500"></div>
      
      <!-- Content -->
      <div class="relative p-4">
        <!-- User Info -->
        <div class="flex items-center mb-4 pt-12">
            <el-avatar :size="64" :src="userStore.profile.avatarUrl || 'https://i.pravatar.cc/150?u=a042581f4e29026704d'" />
            <div class="ml-4 text-white">
              <h2 class="text-xl font-bold">{{ userStore.profile.nickname }}</h2>
              <p class="text-sm opacity-80">用户名: {{ userStore.profile.username }}</p>
            </div>
        </div>

        <!-- My Trades Card -->
        <el-card class="mb-4">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-bold">我的交易</span>
              <el-button text @click="() => router.push('/user/orders')">查看全部订单 ></el-button>
            </div>
          </template>
          <div class="grid grid-cols-4 text-center text-sm text-gray-600">
            <div @click="() => router.push('/user/products')" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1"><Goods /></el-icon>
              <span>我的发布</span>
            </div>
            <div @click="() => router.push({ path: '/user/orders', query: { tab: 'sold' } })" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1"><Sell /></el-icon>
              <span>我卖出的</span>
            </div>
            <div @click="() => router.push({ path: '/user/orders', query: { tab: 'bought' } })" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1"><ShoppingCart /></el-icon>
              <span>我买到的</span>
            </div>
            <div @click="() => router.push({ path: '/user/orders', query: { tab: 'bought', filter: 'completed' } })" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1"><ChatLineSquare /></el-icon>
              <span>待评价</span>
            </div>
          </div>
        </el-card>

        <!-- Other Tools Card -->
        <el-card>
          <template #header>
            <div class="font-bold">我的工具</div>
          </template>
          <div class="grid grid-cols-5 gap-y-4 text-center text-sm text-gray-600">
            <div @click="() => router.push('/user/favorites')" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1 text-red-500"><Star /></el-icon>
              <span>我的收藏</span>
            </div>
            <div @click="() => router.push('/user/profile')" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1 text-blue-500"><User /></el-icon>
              <span>个人资料</span>
            </div>
            <div @click="() => router.push('/user/account')" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1 text-green-500"><Wallet /></el-icon>
              <span>我的账户</span>
            </div>
            <div @click="() => router.push('/user/products?tab=drafts')" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1 text-yellow-500"><EditPen /></el-icon>
              <span>我的草稿</span>
            </div>
            <div @click="contactSupport" class="flex flex-col items-center justify-center cursor-pointer">
              <el-icon :size="28" class="mb-1 text-teal-500"><Service /></el-icon>
              <span>联系客服</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import { Goods, Sell, ShoppingCart, ChatLineSquare, Star, User, Wallet, EditPen, Service } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const contactSupport = () => {
    const conversationId = userStore.getOrCreateRandomSupportConversation();
    router.push(`/chat/${conversationId}`);
}

</script>

<style scoped>
.el-button--text {
  padding: 0;
  height: auto;
  color: #9ca3af;
  font-weight: 400;
}
</style>
