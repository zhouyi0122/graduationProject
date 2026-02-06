<template>
  <div class="fixed inset-x-0 bottom-0 bg-white border-t border-gray-200 shadow-lg z-50">
    <div class="grid grid-cols-5 h-16">
      <!-- 首页 -->
      <router-link to="/" class="flex flex-col items-center justify-center text-gray-500 hover:text-orange-500 transition-colors">
        <HomeIcon class="h-6 w-6" />
        <span class="text-xs">首页</span>
      </router-link>

      <!-- 分类/浏览 -->
      <router-link to="/products" class="flex flex-col items-center justify-center text-gray-500 hover:text-orange-500 transition-colors">
        <Squares2X2Icon class="h-6 w-6" />
        <span class="text-xs">分类</span>
      </router-link>

      <!-- 卖闲置 (发布) -->
      <div class="flex items-center justify-center">
        <button @click="() => router.push('/products/new')" class="h-14 w-14 bg-orange-500 text-white rounded-full shadow-lg flex items-center justify-center transform hover:scale-105 transition-transform focus:outline-none">
          <PlusIcon class="h-8 w-8" />
        </button>
      </div>

      <!-- 消息 -->
      <router-link to="/chat" class="flex flex-col items-center justify-center text-gray-500 hover:text-orange-500 transition-colors">
        <ChatBubbleOvalLeftEllipsisIcon class="h-6 w-6" />
        <span class="text-xs">消息</span>
      </router-link>

      <!-- 我的 -->
      <router-link to="/user" class="flex flex-col items-center justify-center text-gray-500 hover:text-orange-500 transition-colors">
        <UserCircleIcon class="h-6 w-6" />
        <span class="text-xs">我的</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
// Using the 'outline' version for a lighter feel, and 'solid' for the plus icon
import { HomeIcon, Squares2X2Icon, ChatBubbleOvalLeftEllipsisIcon, UserCircleIcon, PlusIcon } from '@heroicons/vue/24/solid';

const router = useRouter();
</script>

<style scoped>
/* 确保路由激活时图标和文字变色 */
.router-link-active,
.router-link-exact-active {
  color: #f97316; /* 淘宝橙 */
}

/* 调整发布按钮的位置，使其稍微突出 */
button {
    transform: translateY(-12px);
}
</style>
