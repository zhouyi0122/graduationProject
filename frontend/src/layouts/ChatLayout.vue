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
    <main :class="{ 'pb-32': isManaging, 'pb-16': !isManaging }">
        <div v-for="convo in displayedConversations" :key="convo.id" 
            class="flex items-center p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50"
            @click="handleItemClick(convo)">
        
          <el-checkbox v-if="isManaging" :model-value="selectedConversations.includes(convo.id)" @click.stop @change="() => toggleSelection(convo.id)" size="large" />
          
          <el-avatar v-if="convo.user.avatarUrl" :size="48" :src="convo.user.avatarUrl" class="ml-4" />
          <div v-else class="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center ml-4">
              <el-icon :size="24" color="white"><Bell /></el-icon>
          </div>

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

    <!-- Deletion Bar -->
    <footer v-if="isManaging" class="fixed inset-x-0 bottom-16 bg-white border-t h-16 flex items-center justify-between px-4 z-50">
        <el-checkbox :model-value="isAllSelected" @change="toggleSelectAll">全选</el-checkbox>
        <el-button type="danger" :disabled="selectedConversations.length === 0" @click="deleteSelected">删除 ({{ selectedConversations.length }})</el-button>
    </footer>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
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

const displayedConversations = computed(() => {
    const baseConversations = authStore.isAdmin ? userStore.allConversations : userStore.conversations;
    if (!searchQuery.value) {
        return baseConversations;
    }
    return baseConversations.filter(convo => 
        convo.user.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        (convo.lastMessage && convo.lastMessage.toLowerCase().includes(searchQuery.value.toLowerCase()))
    );
});

const isAllSelected = computed(() => selectedConversations.value.length === displayedConversations.value.length && displayedConversations.value.length > 0);

const toggleManagementMode = () => {
    isManaging.value = !isManaging.value;
    selectedConversations.value = []; // Reset selection when exiting management mode
}

const handleItemClick = (conversation) => {
    if (isManaging.value) {
        toggleSelection(conversation.id);
    } else {
        if (conversation.id === 'system-support') {
            router.push('/chat/support');
        } else if (conversation.isSystem) {
            const type = conversation.id.split('-')[1];
            router.push(`/chat/system/${type}`);
        } else {
            router.push(`/chat/${conversation.id}`);
        }
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
        userStore.deleteConversations(selectedConversations.value);
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
