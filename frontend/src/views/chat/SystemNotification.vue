<template>
  <div class="system-notification-page bg-gray-100 min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm">
      <div class="relative flex items-center justify-center h-14 px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="router.back()" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">{{ title }}</h1>
      </div>
    </header>

    <!-- Notification Content -->
    <div class="p-4 space-y-4">
        <div v-if="notifications.length > 0">
            <el-card v-for="item in notifications" :key="item.id">
                <template #header>
                    <div class="flex justify-between items-center">
                        <span class="font-bold">{{ item.title }}</span>
                        <span class="text-xs text-gray-400">{{ item.timestamp }}</span>
                    </div>
                </template>
                <p>{{ item.content }}</p>
            </el-card>
        </div>
        <el-empty v-else description="暂无更多消息"></el-empty>
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const title = computed(() => {
    if (route.params.type === 'notification') {
        return '通知消息';
    }
    return '系统消息';
});

const notifications = computed(() => {
    if (route.params.type === 'notification') {
        return userStore.notifications;
    }
    return [];
});

</script>
