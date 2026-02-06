<template>
  <div class="admin-chat-page h-full flex flex-col">
    <h1 class="text-2xl font-bold mb-4 flex-shrink-0">消息中心</h1>
    
    <el-tabs v-model="activeTab" class="flex-grow flex flex-col custom-tabs">
      <el-tab-pane label="用户私信" name="directMessages" class="h-full">
        <el-card class="h-full flex p-0" body-style="display: flex; width: 100%; height: 100%; padding: 0;">
          <div class="w-1/3 border-r overflow-y-auto">
            <div v-for="convo in allConversations" :key="convo.id"
                class="flex items-center p-3 cursor-pointer border-b border-gray-100"
                :class="{ 'bg-orange-100': activeConversation && activeConversation.id === convo.id }"
                @click="selectConversation(convo)">
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
          </div>
          <div class="w-2/3 flex flex-col">
            <div v-if="activeConversation" class="h-full flex flex-col">
              <header class="h-16 flex-shrink-0 border-b flex items-center px-6">
                <h2 class="text-lg font-semibold">{{ activeConversation.user.name }}</h2>
              </header>
              <main ref="messageContainer" class="flex-grow overflow-y-auto p-6 space-y-4 bg-gray-50">
                <div v-for="message in activeConversation.messages" :key="message.id"
                    :class="['flex items-end gap-2', message.sender === 'me' ? 'flex-row-reverse' : '']">
                  <el-avatar :src="message.sender === 'me' ? (userStore.profile.avatarUrl || 'https://i.pravatar.cc/40?u=admin') : activeConversation.user.avatarUrl" />
                  <div class="flex flex-col" :class="[message.sender === 'me' ? 'items-end' : 'items-start']">
                      <div :class="['max-w-md px-4 py-2 rounded-lg',
                                  message.sender === 'me' ? 'bg-orange-500 text-white' : 'bg-white text-gray-800 shadow-sm']">
                        <p>{{ message.text }}</p>
                      </div>
                      <p v-if="message.sender === 'me'" class="text-xs text-gray-400 mt-1 px-1">{{ message.read ? '已读' : '未读' }}</p>
                  </div>
                </div>
              </main>
              <footer class="flex-shrink-0 p-4 border-t bg-white">
                <div class="flex items-center">
                  <el-input v-model="newMessage" placeholder="输入消息..." @keyup.enter="sendMessage" class="flex-grow" size="large" />
                  <el-button type="primary" @click="sendMessage" class="ml-3" size="large">发送</el-button>
                </div>
              </footer>
            </div>
            <el-empty v-else description="请选择一个对话开始聊天" class="m-auto"></el-empty>
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="发送通知" name="sendNotification" class="h-full">
        <div class="grid grid-cols-2 gap-6 h-full">
            <el-card>
                <template #header><div class="font-bold">发送新通知</div></template>
                <el-form :model="notificationForm" label-position="top">
                    <el-form-item label="通知标题">
                        <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
                    </el-form-item>
                    <el-form-item label="通知内容">
                        <el-input v-model="notificationForm.content" type="textarea" :rows="8" placeholder="请输入通知内容"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="sendNotification">立即发送</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-card>
                <template #header><div class="font-bold">历史通知</div></template>
                <el-timeline>
                    <el-timeline-item v-for="item in userStore.notifications" :key="item.id" :timestamp="item.timestamp">
                        <h4 class="font-semibold">{{ item.title }}</h4>
                        <p class="text-sm text-gray-600">{{ item.content }}</p>
                    </el-timeline-item>
                </el-timeline>
            </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch } from 'vue';
import { useUserStore } from '../../stores/user.store';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();

const activeTab = ref('directMessages');
const allConversations = computed(() => userStore.allConversations);
const activeConversation = ref(null);
const newMessage = ref('');
const messageContainer = ref(null);

const notificationForm = reactive({
    title: '',
    content: ''
});

const selectConversation = (convo) => {
  activeConversation.value = convo;
  userStore.markConversationAsRead(convo.id);
  scrollToBottom();
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
    });
}

watch(() => activeConversation.value?.messages, () => {
    scrollToBottom();
}, { deep: true });

const sendMessage = () => {
    if (!newMessage.value.trim() || !activeConversation.value) return;
    userStore.addMessage(activeConversation.value.id, newMessage.value);
    newMessage.value = '';
}

const sendNotification = () => {
    if (!notificationForm.title.trim() || !notificationForm.content.trim()) {
        ElMessage.warning('标题和内容不能为空');
        return;
    }
    userStore.sendNotification({ ...notificationForm });
    ElMessage.success('通知发送成功！');
    notificationForm.title = '';
    notificationForm.content = '';
}

</script>

<style scoped>
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.el-card {
    height: 100%;
}
.el-tabs {
    display: flex;
    flex-direction: column;
}
:deep(.el-tabs__content) {
    flex-grow: 1;
}
:deep(.el-tab-pane) {
    height: 100%;
}
.custom-tabs :deep(.el-tabs__item.is-active) {
    color: #f97316;
}

.custom-tabs :deep(.el-tabs__item:hover) {
    color: #f97316;
}

.custom-tabs :deep(.el-tabs__active-bar) {
    background-color: #f97316;
}
:deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px #f97316 inset !important;
}
</style>
