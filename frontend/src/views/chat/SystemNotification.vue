<template>
  <div class="system-notification-page bg-gray-100 min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm flex-shrink-0">
      <div class="h-14 flex items-center justify-center relative px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">系统通知</h1>
      </div>
    </header>

    <!-- Notification List -->
    <main class="p-4 space-y-4" v-loading="loading">
        <div v-if="userStore.notifications.length > 0">
            <div v-for="notif in userStore.notifications" :key="notif.id" 
                class="bg-white rounded-xl p-4 shadow-sm">
                <div class="flex items-center justify-between mb-2">
                    <h3 class="font-bold text-gray-800">{{ notif.title }}</h3>
                    <span class="text-[10px] text-gray-400">{{ formatDate(notif.createTime) }}</span>
                </div>
                <p class="text-sm text-gray-600 leading-relaxed">{{ notif.content }}</p>
            </div>
        </div>
        <el-empty v-else description="暂无系统通知" />
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

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

onMounted(async () => {
    loading.value = true;
    try {
        await userStore.fetchNotifications();
        // 进入页面即标记为已读
        if (userStore.unreadNotificationsCount > 0) {
            await userStore.markNotificationsAsRead();
        }
    } catch (error) {
        ElMessage.error('加载通知失败');
    } finally {
        loading.value = false;
    }
});
</script>

<style scoped>
.system-notification-page {
    max-width: 100%;
}
</style>
