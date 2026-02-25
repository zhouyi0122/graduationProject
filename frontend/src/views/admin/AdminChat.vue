<template>
  <div class="admin-chat-page h-full flex flex-col">
    <h1 class="text-2xl font-bold mb-4 flex-shrink-0">消息中心</h1>
    
    <el-tabs v-model="activeTab" class="flex-grow flex flex-col custom-tabs">
      <el-tab-pane label="用户私信" name="directMessages" class="h-full">
        <el-card class="h-full flex p-0" body-style="display: flex; width: 100%; height: 100%; padding: 0;">
          <!-- 左侧会话列表 -->
          <div class="w-1/3 border-r overflow-y-auto" v-loading="loadingList">
            <div v-if="userStore.conversations.length > 0">
              <div v-for="convo in userStore.conversations" :key="convo.id"
                  class="flex items-center p-3 cursor-pointer border-b border-gray-100 transition-colors"
                  :class="{ 'bg-orange-50': activeConversation && activeConversation.id === convo.id }"
                  @click="selectConversation(convo)">
                <div class="relative flex-shrink-0">
                  <el-avatar :size="48" :src="convo.otherUser?.avatar || `https://picsum.photos/100/100?random=u${convo.otherUser?.id}`" />
                  <!-- 管理员端消息红点 -->
                  <div v-if="convo.unreadCount > 0" 
                       style="position: absolute !important; top: -5px !important; right: -5px !important; background-color: #ff4d4f !important; color: white !important; font-size: 12px !important; min-width: 18px !important; height: 18px !important; border-radius: 9px !important; display: flex !important; align-items: center !important; justify-content: center !important; padding: 0 4px !important; border: 2px solid white !important; z-index: 10 !important; font-weight: bold !important; box-shadow: 0 2px 4px rgba(0,0,0,0.2) !important;">
                      {{ convo.unreadCount > 99 ? '99+' : convo.unreadCount }}
                  </div>
                </div>
                <div class="flex-grow ml-3 min-w-0">
                  <div class="flex justify-between items-center">
                    <div class="flex items-center gap-2 min-w-0">
                      <p class="font-semibold text-gray-800 truncate">{{ convo.otherUser?.nickname || '用户' }}</p>
                    </div>
                    <p class="text-[10px] text-gray-400 flex-shrink-0">{{ formatTime(convo.lastTime) }}</p>
                  </div>
                  <div class="flex justify-between items-start mt-1">
                    <p class="text-sm text-gray-500 truncate">{{ formatLastMessage(convo.lastMessage) }}</p>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无私信内容" />
          </div>

          <!-- 右侧聊天窗口 -->
          <div class="w-2/3 flex flex-col bg-gray-50">
            <div v-if="activeConversation" class="h-full flex flex-col">
              <header class="h-16 flex-shrink-0 border-b bg-white flex items-center px-6">
                <h2 class="text-lg font-semibold">{{ activeConversation.otherUser?.nickname }}</h2>
              </header>
              
              <main ref="messageContainer" class="flex-grow overflow-y-auto p-6 space-y-4">
                <div v-for="msg in currentMessages" :key="msg.id"
                    :class="['flex items-start gap-2', isMe(msg.senderId) ? 'flex-row-reverse' : '']">
                  <el-avatar :size="40" :src="isMe(msg.senderId) ? (userStore.profile.avatar || `https://picsum.photos/40?random=me`) : (activeConversation.otherUser?.avatar || `https://picsum.photos/40?random=u${activeConversation.otherUser?.id}`)" />
                  <div class="flex flex-col" :class="[isMe(msg.senderId) ? 'items-end' : 'items-start']">
                      <!-- 1. 商品小卡片消息 -->
                      <div v-if="isProductCard(msg.content)"
                           @click="goToProductDetail(parseProductCard(msg.content).id)"
                           class="max-w-xs bg-white rounded-xl overflow-hidden shadow-md border border-gray-100 mb-1 cursor-pointer hover:shadow-lg active:scale-95 transition-all">
                        <img :src="parseProductCard(msg.content).imageUrl" class="w-full h-32 object-cover" />
                        <div class="p-3">
                          <p class="text-sm font-bold text-gray-800 line-clamp-2 mb-2">{{ parseProductCard(msg.content).title }}</p>
                          <div class="flex justify-between items-center">
                            <span class="text-base font-bold text-red-500">¥{{ parseProductCard(msg.content).price }}</span>
                            <span class="text-[10px] text-gray-400">查看详情</span>
                          </div>
                        </div>
                      </div>

                      <!-- 2. 订单小卡片消息 -->
                      <div v-else-if="isOrderCard(msg.content)"
                           @click="goToOrderDetail(parseOrderCard(msg.content).id)"
                           class="max-w-xs bg-white rounded-xl overflow-hidden shadow-md border border-gray-100 mb-1 cursor-pointer hover:shadow-lg active:scale-95 transition-all">
                        <div class="p-3 border-b border-gray-50 bg-gray-50/50 flex justify-between items-center">
                          <span class="text-[10px] text-gray-500">订单编号: {{ parseOrderCard(msg.content).orderNumber }}</span>
                          <span class="text-[10px] text-orange-500">查看详情</span>
                        </div>
                        <div class="p-3 flex space-x-3">
                          <img :src="parseOrderCard(msg.content).imageUrl" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                          <div class="flex-grow min-w-0 flex flex-col justify-between py-0.5">
                            <p class="text-xs font-bold text-gray-800 line-clamp-2">{{ parseOrderCard(msg.content).title }}</p>
                            <span class="text-sm font-bold text-red-500">¥{{ parseOrderCard(msg.content).price }}</span>
                          </div>
                        </div>
                      </div>

                      <!-- 3. 普通文本消息 -->
                      <div v-else :class="['max-w-md px-4 py-2 rounded-xl shadow-sm',
                                  isMe(msg.senderId) ? 'bg-orange-500 text-white rounded-tr-none' : 'bg-white text-gray-800 rounded-tl-none']">
                        <p class="text-sm whitespace-pre-wrap break-all">{{ msg.content }}</p>
                      </div>
                      <p class="text-[10px] text-gray-400 mt-1 px-1">{{ formatDate(msg.createTime) }}</p>
                  </div>
                </div>
              </main>

              <footer class="flex-shrink-0 p-4 border-t bg-white">
                <div class="flex items-center">
                  <el-input v-model="newMessage" placeholder="输入回复内容..." @keyup.enter="handleSendMessage" class="flex-grow" size="large" />
                  <el-button type="primary" @click="handleSendMessage" :loading="sending" class="ml-3" size="large">发送</el-button>
                </div>
              </footer>
            </div>
            <el-empty v-else description="请选择一个对话开始回复" class="m-auto"></el-empty>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 系统通知标签页 -->
      <el-tab-pane label="发送通知" name="sendNotification" class="h-full">
        <div class="grid grid-cols-2 gap-6 h-full p-4 overflow-y-auto">
            <el-card shadow="never">
                <template #header><div class="font-bold">发送新通知</div></template>
                <el-form :model="notificationForm" label-position="top">
                    <el-form-item label="通知标题">
                        <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
                    </el-form-item>
                    <el-form-item label="通知内容">
                        <el-input v-model="notificationForm.content" type="textarea" :rows="8" placeholder="请输入通知内容"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" :loading="sendingNotification" @click="handleSendNotification">立即发送</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-card shadow="never" class="flex flex-col overflow-hidden">
                <template #header><div class="font-bold">历史通知</div></template>
                <div class="overflow-y-auto pr-2" style="max-height: 500px;">
                    <el-timeline v-if="userStore.notifications.length > 0">
                        <el-timeline-item v-for="item in userStore.notifications" :key="item.id" :timestamp="formatDate(item.createTime)" placement="top">
                            <el-card shadow="hover" size="small">
                                <h4 class="font-semibold text-sm">{{ item.title }}</h4>
                                <p class="text-xs text-gray-600 mt-2 leading-relaxed">{{ item.content }}</p>
                            </el-card>
                        </el-timeline-item>
                    </el-timeline>
                    <el-empty v-else description="暂无历史通知" />
                </div>
            </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { useAuthStore } from '../../stores/auth.store';
import apiClient from '../../services/api';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();

const activeTab = ref('directMessages');
const activeConversation = ref(null);
const currentMessages = ref([]);
const newMessage = ref('');
const messageContainer = ref(null);
const loadingList = ref(false);
const sending = ref(false);
const sendingNotification = ref(false);
let refreshInterval = null;

const notificationForm = reactive({ title: '', content: '' });

const isMe = (senderId) => String(senderId) === String(authStore.user?.id);

// 商品卡片逻辑
const isProductCard = (content) => content && content.startsWith('[PRODUCT_CARD]');
const parseProductCard = (content) => {
    try { return JSON.parse(content.replace('[PRODUCT_CARD]', '')); } catch (e) { return {}; }
}

// 订单卡片逻辑
const isOrderCard = (content) => content && content.startsWith('[ORDER_CARD]');
const parseOrderCard = (content) => {
    try { return JSON.parse(content.replace('[ORDER_CARD]', '')); } catch (e) { return {}; }
}

const goToProductDetail = (productId) => {
    router.push(`/product/${productId}`);
}

const goToOrderDetail = (orderId) => {
    router.push(`/admin/orders/${orderId}`);
}

const formatLastMessage = (content) => {
    if (!content) return '暂无消息';
    if (content.startsWith('[PRODUCT_CARD]')) return '[商品信息]';
    if (content.startsWith('[ORDER_CARD]')) return '[订单信息]';
    return content;
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

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-CN', { hour12: false });
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
    });
}

const fetchConversations = async () => {
    try {
        await userStore.fetchConversations();
    } catch (error) {
        console.error('获取列表失败:', error);
    }
};

const selectConversation = async (convo) => {
    activeConversation.value = convo;
    try {
        const msgs = await userStore.fetchMessages(convo.id);
        currentMessages.value = msgs;
        
        // 标记已读
        if (convo.unreadCount > 0) {
            await userStore.markConversationAsRead(convo.id);
        }
        
        scrollToBottom();
    } catch (error) {
        ElMessage.error('加载消息失败');
    }
}

const handleSendMessage = async () => {
    if (!newMessage.value.trim() || !activeConversation.value || sending.value) return;
    
    sending.value = true;
    try {
        await userStore.sendChatMessage(activeConversation.value.id, newMessage.value);
        newMessage.value = '';
        // 立即局部刷新消息
        const msgs = await userStore.fetchMessages(activeConversation.value.id);
        currentMessages.value = msgs;
        scrollToBottom();
        // 同时刷新左侧列表预览
        fetchConversations();
    } catch (error) {
        ElMessage.error('发送失败');
    } finally {
        sending.value = false;
    }
}

const handleSendNotification = async () => {
    if (!notificationForm.title.trim() || !notificationForm.content.trim()) {
        ElMessage.warning('标题和内容不能为空');
        return;
    }
    
    sendingNotification.value = true;
    try {
        await userStore.sendAdminNotification({
            title: notificationForm.title,
            content: notificationForm.content
        });
        ElMessage.success('通知发送成功！');
        notificationForm.title = '';
        notificationForm.content = '';
    } catch (error) {
        ElMessage.error('发送失败，请稍后再试');
    } finally {
        sendingNotification.value = false;
    }
}

onMounted(async () => {
    loadingList.value = true;
    // 同时拉取会话列表和通知列表
    await Promise.all([
        fetchConversations(),
        userStore.fetchAdminNotifications()
    ]);
    loadingList.value = false;
    
    // 设置 5 秒轮询，实时获取新私信
    refreshInterval = setInterval(() => {
        fetchConversations();
        if (activeConversation.value) {
            userStore.fetchMessages(activeConversation.value.id).then(msgs => {
                currentMessages.value = msgs;
            });
        }
    }, 5000);
});

onUnmounted(() => {
    if (refreshInterval) clearInterval(refreshInterval);
});

watch(() => currentMessages.value?.length, () => {
    scrollToBottom();
});
</script>

<style scoped>
.admin-chat-page {
    height: calc(100vh - 120px);
}
.custom-tabs {
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
.custom-tabs :deep(.el-tabs__active-bar) {
    background-color: #f97316;
}
</style>
