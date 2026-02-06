<template>
  <div class="support-list-page bg-white min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm">
      <div class="h-14 flex items-center justify-center relative px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">官方客服</h1>
      </div>
    </header>

    <!-- Support Conversations List -->
    <main>
        <div v-for="convo in supportConversations" :key="convo.id" 
            class="flex items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
            @click="openChat(convo.id)">
        
        <el-avatar :size="48" :src="convo.user.avatarUrl" />

        <div class="flex-grow ml-3 min-w-0">
            <div class="flex justify-between items-center">
            <p class="font-semibold text-gray-800 truncate">{{ convo.user.name }}</p>
            <p class="text-xs text-gray-400 flex-shrink-0">{{ convo.timestamp }}</p>
            </div>
            <div class="flex justify-between items-start mt-1">
            <p class="text-sm text-gray-500 truncate">{{ convo.lastMessage }}</p>
            <el-badge :value="convo.unreadCount" :max="99" :hidden="convo.unreadCount === 0" class="ml-2" />
            </div>
        </div>
        </div>
    </main>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const supportConversations = computed(() => userStore.supportConversations);

const openChat = (id) => {
    router.push(`/chat/${id}`);
}

</script>

<style scoped>
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
