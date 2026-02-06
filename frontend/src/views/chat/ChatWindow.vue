<template>
  <div class="chat-window-page flex flex-col h-screen bg-gray-100">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm flex-shrink-0">
      <div class="h-14 flex items-center justify-center relative px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="goBack" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">{{ conversation?.user.name }}</h1>
      </div>
    </header>

    <!-- Chat Messages -->
    <main ref="messageContainer" class="flex-grow overflow-y-auto p-4 space-y-4">
      <div v-for="message in conversation?.messages" :key="message.id"
           :class="['flex items-end gap-2', message.sender === 'me' ? 'flex-row-reverse' : '']">
        
        <el-avatar :src="message.sender === 'me' ? (userStore.profile.avatarUrl || 'https://i.pravatar.cc/150?u=a042581f4e29026704d') : conversation.user.avatarUrl" />
        
        <div class="flex flex-col" :class="[message.sender === 'me' ? 'items-end' : 'items-start']">
            <div :class="['max-w-xs lg:max-w-md px-4 py-2 rounded-lg',
                        message.sender === 'me' ? 'bg-orange-500 text-white' : 'bg-white text-gray-800 shadow-sm']">
            <p>{{ message.text }}</p>
            </div>
            <p v-if="message.sender === 'me'" class="text-xs text-gray-400 mt-1 px-1">{{ message.read ? '已读' : '未读' }}</p>
        </div>
      </div>
    </main>

    <!-- Message Input -->
    <footer class="flex-shrink-0 bg-white p-2 border-t">
      <div class="flex items-center">
        <el-input
          v-model="newMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          class="flex-grow"
        />
        <el-button type="primary" @click="sendMessage" class="ml-2">发送</el-button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const newMessage = ref('');
const messageContainer = ref(null);

const conversationId = computed(() => {
    return isNaN(parseInt(route.params.id, 10)) ? route.params.id : parseInt(route.params.id, 10);
});

const conversation = computed(() => {
    const allConversations = [...userStore.conversations, ...userStore.supportConversations];
    return allConversations.find(c => c.id == conversationId.value);
});

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
    });
}

onMounted(() => {
    if (conversationId.value) {
        userStore.markConversationAsRead(conversationId.value);
    }
    scrollToBottom();
});

watch(() => conversation.value?.messages, () => {
    scrollToBottom();
}, { deep: true });

const goBack = () => {
    window.history.go(-1);
};

const sendMessage = () => {
    if (!newMessage.value.trim()) return;
    userStore.addMessage(conversation.value.id, newMessage.value);
    newMessage.value = '';
}

</script>
