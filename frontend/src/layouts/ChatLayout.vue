<template>
  <div class="chat-layout-page bg-white min-h-screen">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white">
      <div class="h-14 flex items-center justify-between px-4 text-gray-800">
        <h1 class="text-xl font-bold">消息</h1>
        <div v-if="!authStore.isAdmin" class="flex items-center space-x-4 text-sm text-gray-600">
            <button @click="toggleManagementMode" class="font-semibold">{{ isManaging ? '完成' : '管理' }}</button>
        </div>
      </div>
      <div class="px-4 pb-2">
        <el-input
          v-model="searchQuery"
          placeholder="搜索聊天记录"
          :prefix-icon="Search"
        />
      </div>
    </header>

    <!-- Message List -->
    <main :class="{ 'pb-32': isManaging, 'pb-16': !isManaging }" v-loading="loading">
        <!-- 固定系统通知入口 -->
        <div v-if="!isManaging" 
             class="flex items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
             @click="router.push('/chat/system/all')">
          <div class="relative flex-shrink-0 ml-2">
            <div class="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center">
                <el-icon :size="24" color="white"><Bell /></el-icon>
            </div>
            <!-- 系统通知未读红点 -->
            <div v-if="userStore.unreadNotificationsCount > 0" 
                 style="position: absolute !important; top: -5px !important; right: -5px !important; background-color: #ff4d4f !important; color: white !important; font-size: 12px !important; min-width: 18px !important; height: 18px !important; border-radius: 9px !important; display: flex !important; align-items: center !important; justify-content: center !important; padding: 0 4px !important; border: 2px solid white !important; z-index: 50 !important; font-weight: bold !important; box-shadow: 0 2px 4px rgba(0,0,0,0.2) !important;">
                {{ userStore.unreadNotificationsCount > 99 ? '99+' : userStore.unreadNotificationsCount }}
            </div>
          </div>
          <div class="flex-grow ml-3 min-w-0">
              <div class="flex justify-between items-center">
                <div class="flex items-center gap-2">
                  <p class="font-semibold text-gray-800 truncate">系统通知</p>
                  <span v-if="userStore.unreadNotificationsCount > 0" class="text-red-500 font-bold text-xs bg-red-50 px-1 rounded">
                      ({{ userStore.unreadNotificationsCount }}条未读)
                  </span>
                </div>
                <p v-if="latestNotif" class="text-xs text-gray-400 flex-shrink-0">{{ formatTime(latestNotif.createTime) }}</p>
              </div>
              <p class="text-sm text-gray-500 truncate">{{ latestNotif ? latestNotif.title : '查看平台最新公告与消息' }}</p>
          </div>
        </div>

        <div v-if="displayedConversations.length > 0">
            <div v-for="convo in displayedConversations" :key="convo.id" 
                class="flex items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
                @click="handleItemClick(convo)">
            
              <el-checkbox v-if="isManaging" :model-value="selectedConversations.includes(convo.id)" @click.stop @change="() => toggleSelection(convo.id)" size="large" />
              
              <div class="relative flex-shrink-0 ml-2">
                <el-avatar :size="48" :src="convo.otherUser?.avatar || `https://picsum.photos/100/100?random=u${convo.otherUser?.id}`" />
                <!-- 消息红点显示 -->
                <div v-if="convo.unreadCount > 0" 
                     class="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] min-w-[18px] h-[18px] flex items-center justify-center rounded-full px-1 border-2 border-white z-10 font-bold shadow-sm">
                    {{ convo.unreadCount > 99 ? '99+' : convo.unreadCount }}
                </div>
              </div>

              <div class="flex-grow ml-3 min-w-0">
                  <div class="flex justify-between items-center">
                    <p class="font-semibold text-gray-800 truncate">{{ convo.otherUser?.nickname || '闲置用户' }}</p>
                    <p class="text-xs text-gray-400 flex-shrink-0">{{ formatTime(convo.lastTime) }}</p>
                  </div>
                  <div class="flex justify-between items-start mt-1">
                    <p class="text-sm text-gray-500 truncate">{{ formatLastMessage(convo.lastMessage) }}</p>
                  </div>
              </div>
            </div>
        </div>
        <el-empty v-else description="暂无消息对话" />
    </main>

    <!-- Deletion Bar -->
    <footer v-if="isManaging" class="fixed inset-x-0 bottom-16 bg-white border-t h-16 flex items-center justify-between px-4 z-50">
        <el-checkbox :model-value="isAllSelected" @change="toggleSelectAll">全选</el-checkbox>
        <el-button type="danger" :disabled="selectedConversations.length === 0" @click="deleteSelected">删除 ({{ selectedConversations.length }})</el-button>
    </footer>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import { useAuthStore } from '../stores/auth.store';
import { Search, Bell } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();

const isManaging = ref(false);
const selectedConversations = ref([]);
const searchQuery = ref('');
const loading = ref(false);
let refreshInterval = null;

const latestNotif = computed(() => {
    return userStore.notifications.length > 0 ? userStore.notifications[0] : null;
});

const formatLastMessage = (content) => {
    if (!content) return '暂无消息';
    if (content.startsWith('[PRODUCT_CARD]')) return '[商品信息]';
    if (content.startsWith('[ORDER_CARD]')) return '[订单信息]';
    return content;
}

const displayedConversations = computed(() => {
    const baseConversations = userStore.conversations;
    if (!searchQuery.value) {
        return baseConversations;
    }
    return baseConversations.filter(convo => 
        convo.otherUser?.nickname?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        (convo.lastMessage && convo.lastMessage.toLowerCase().includes(searchQuery.value.toLowerCase()))
    );
});

const isAllSelected = computed(() => selectedConversations.value.length === displayedConversations.value.length && displayedConversations.value.length > 0);

const formatTime = (timeStr) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    if (date.toDateString() === now.toDateString()) {
        return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
    }
    return (date.getMonth() + 1) + '-' + date.getDate();
}

const fetchList = async (showLoading = false) => {
    if (showLoading) loading.value = true;
    try {
        await Promise.all([
            userStore.fetchConversations(),
            userStore.fetchNotifications()
        ]);
    } catch (error) {
        console.error('获取数据失败:', error);
    } finally {
        if (showLoading) loading.value = false;
    }
};

onMounted(() => {
    fetchList(true);
    // 设置每 5 秒自动刷新一次列表
    refreshInterval = setInterval(() => {
        fetchList(false);
    }, 5000);
});

onUnmounted(() => {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
});

const toggleManagementMode = () => {
    isManaging.value = !isManaging.value;
    selectedConversations.value = []; 
}

const handleItemClick = (conversation) => {
    if (isManaging.value) {
        toggleSelection(conversation.id);
    } else {
        router.push(`/chat/${conversation.id}`);
    }
}

const toggleSelection = (id) => {
    const index = selectedConversations.value.indexOf(id);
    if (index > -1) {
        selectedConversations.value.splice(index, 1);
    } else {
        selectedConversations.value.push(id);
    }
}

const toggleSelectAll = () => {
    if (isAllSelected.value) {
        selectedConversations.value = [];
    } else {
        selectedConversations.value = displayedConversations.value.map(c => c.id);
    }
}

const deleteSelected = () => {
    if (selectedConversations.value.length === 0) return;
    ElMessageBox.confirm(`确定要删除这 ${selectedConversations.value.length} 条会话吗？`, '提示', { type: 'warning' })
    .then(() => {
        // 后续对接删除接口
        ElMessage.success('删除成功');
        selectedConversations.value = [];
        isManaging.value = false;
    }).catch(() => {});
}

</script>

<style scoped>
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-checkbox__inner) {
  border-radius: 50%;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #f97316; /* orange-500 */
  border-color: #f97316;
}

:deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #f97316;
}

:deep(.el-checkbox__input .el-checkbox__inner:hover) {
  border-color: #f97316;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: white;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: #f97316;
}

:deep(.el-checkbox__input.is-focus) {
    box-shadow: none !important;
}
:deep(.el-checkbox__inner:focus) {
    box-shadow: none !important;
}
</style>
