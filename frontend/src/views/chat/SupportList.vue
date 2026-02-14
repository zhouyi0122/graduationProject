<template>
  <div class="support-list-page bg-white min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm flex-shrink-0">
      <div class="h-14 flex items-center justify-center relative px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">我的消息</h1>
      </div>
    </header>

    <!-- Conversations List -->
    <main v-loading="loading">
        <div v-if="userStore.conversations.length > 0">
            <div v-for="convo in userStore.conversations" :key="convo.id" 
                class="flex items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
                @click="openChat(convo.id)">
            
            <el-avatar :size="48" :src="convo.otherUser?.avatar || `https://picsum.photos/100/100?random=u${convo.otherUser?.id}`" />

            <div class="flex-grow ml-3 min-w-0">
                <div class="flex justify-between items-center">
                <p class="font-semibold text-gray-800 truncate">{{ convo.otherUser?.nickname || '闲置用户' }}</p>
                <p class="text-xs text-gray-400 flex-shrink-0">{{ formatTime(convo.lastTime) }}</p>
                </div>
                <div class="flex justify-between items-start mt-1">
                <p class="text-sm text-gray-500 truncate">{{ convo.lastMessage || '暂无消息' }}</p>
                </div>
            </div>
            </div>
        </div>
        <el-empty v-else description="暂无消息对话" />
    </main>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

const openChat = (id) => {
    router.push(`/chat/${id}`);
}

const formatTime = (timeStr) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    if (date.toDateString() === now.toDateString()) {
        return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
    }
    return (date.getMonth() + 1) + '-' + date.getDate();
}

onMounted(async () => {
    loading.value = true;
    try {
        await userStore.fetchConversations();
    } catch (error) {
        ElMessage.error('加载消息列表失败');
    } finally {
        loading.value = false;
    }
});

</script>

<style scoped>
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
