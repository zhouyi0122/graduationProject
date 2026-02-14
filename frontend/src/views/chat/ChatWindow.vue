<template>
  <div class="chat-window-page flex flex-col h-screen bg-gray-100">
    <!-- Top Header -->
    <header class="sticky top-0 z-30 bg-white shadow-sm flex-shrink-0">
      <div class="h-14 flex items-center justify-center relative px-4 text-gray-800">
        <div class="absolute left-4">
            <el-icon @click="goBack" :size="20" class="cursor-pointer"><ArrowLeftBold /></el-icon>
        </div>
        <h1 class="text-lg font-semibold">{{ otherUser?.nickname || '聊天中...' }}</h1>
      </div>
    </header>

    <!-- Chat Messages -->
    <main ref="messageContainer" class="flex-grow overflow-y-auto p-4 space-y-4">
      <div v-for="msg in messages" :key="msg.id"
           :class="['flex items-start gap-2', isMe(msg.senderId) ? 'flex-row-reverse' : '']">
        
        <el-avatar :size="40" :src="isMe(msg.senderId) ? (userStore.profile.avatar || `https://picsum.photos/100/100?random=me`) : (otherUser?.avatar || `https://picsum.photos/100/100?random=u${otherUser?.id}`)" />
        
        <div class="flex flex-col" :class="[isMe(msg.senderId) ? 'items-end' : 'items-start']">
            <!-- 1. 商品小卡片消息 -->
            <div v-if="isProductCard(msg.content)" @click="goToProductDetail(parseProductCard(msg.content).id)"
                 class="max-w-[70vw] bg-white rounded-xl overflow-hidden shadow-md border border-gray-100 cursor-pointer hover:shadow-lg active:scale-95 transition-all">
              <img :src="parseProductCard(msg.content).imageUrl" class="w-full h-32 object-cover" />
              <div class="p-3">
                <p class="text-sm font-bold text-gray-800 line-clamp-2 mb-2">{{ parseProductCard(msg.content).title }}</p>
                <div class="flex justify-between items-center">
                  <span class="text-base font-bold text-red-500">¥{{ parseProductCard(msg.content).price }}</span>
                  <span class="text-[10px] text-gray-400 bg-gray-50 px-1.5 py-0.5 rounded border border-gray-100">查看详情</span>
                </div>
              </div>
            </div>

            <!-- 2. 订单小卡片消息 -->
            <div v-else-if="isOrderCard(msg.content)" @click="goToOrderDetail(parseOrderCard(msg.content).id)"
                 class="max-w-[70vw] bg-white rounded-xl overflow-hidden shadow-md border border-gray-100 cursor-pointer hover:shadow-lg active:scale-95 transition-all">
              <div class="p-3 border-b border-gray-50 bg-gray-50/50 flex justify-between items-center">
                <span class="text-[10px] text-gray-500">订单编号: {{ parseOrderCard(msg.content).orderNumber }}</span>
                <el-tag size="small" type="warning" effect="plain">维权相关</el-tag>
              </div>
              <div class="p-3 flex space-x-3">
                <img :src="parseOrderCard(msg.content).imageUrl" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                <div class="flex-grow min-w-0 flex flex-col justify-between py-0.5">
                  <p class="text-xs font-bold text-gray-800 line-clamp-2">{{ parseOrderCard(msg.content).title }}</p>
                  <div class="flex justify-between items-baseline">
                    <span class="text-sm font-bold text-red-500">¥{{ parseOrderCard(msg.content).price }}</span>
                    <span class="text-[10px] text-orange-500">查看详情</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 3. 普通文本消息 -->
            <div v-else 
                 :class="['max-w-[75vw] px-4 py-2 rounded-2xl shadow-sm transition-all',
                        isMe(msg.senderId) ? 'bg-orange-500 text-white rounded-tr-none' : 'bg-white text-gray-800 rounded-tl-none']">
              <p class="text-sm leading-relaxed whitespace-pre-wrap break-all">{{ msg.content }}</p>
            </div>

            <p class="text-[10px] text-gray-400 mt-1 px-1">{{ formatTime(msg.createTime) }}</p>
        </div>
      </div>
    </main>

    <!-- Message Input -->
    <footer class="flex-shrink-0 bg-white p-3 border-t pb-safe">
      <div class="flex items-center gap-2">
        <el-input
          v-model="newMessageContent"
          placeholder="输入消息..."
          @keyup.enter="handleSendMessage"
          class="flex-grow custom-input"
          size="large"
        />
        <el-button type="primary" @click="handleSendMessage" :loading="sending" circle size="large">
          <el-icon :size="20"><Promotion /></el-icon>
        </el-button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { useAuthStore } from '../../stores/auth.store';
import { useProductStore } from '../../stores/product.store';
import apiClient from '../../services/api';
import { ArrowLeftBold, Promotion } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const productStore = useProductStore();

const newMessageContent = ref('');
const messageContainer = ref(null);
const messages = ref([]);
const otherUser = ref(null);
const sending = ref(false);
let refreshInterval = null;

const conversationId = route.params.id;
const initialProductId = route.query.productId;
const initialOrderId = route.query.orderId;
const isFromSupport = route.query.isSupport === 'true';

const isMe = (senderId) => String(senderId) === String(authStore.user?.id);

const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
    });
}

// 商品卡片逻辑
const isProductCard = (content) => content && content.startsWith('[PRODUCT_CARD]');
const parseProductCard = (content) => {
    try { return JSON.parse(content.replace('[PRODUCT_CARD]', '')); } catch (e) { return {}; }
}
const goToProductDetail = (productId) => {
    router.push(`/product/${productId}`);
}

// 订单卡片逻辑
const isOrderCard = (content) => content && content.startsWith('[ORDER_CARD]');
const parseOrderCard = (content) => {
    try { return JSON.parse(content.replace('[ORDER_CARD]', '')); } catch (e) { return {}; }
}
const goToOrderDetail = (orderId) => {
    // 根据角色跳转不同路径
    if (authStore.isAdmin) {
        router.push(`/admin/orders/${orderId}`);
    } else {
        router.push(`/user/order/${orderId}`);
    }
}

const loadChatData = async () => {
    try {
        const msgs = await userStore.fetchMessages(conversationId);
        messages.value = msgs;
        
        if (!otherUser.value) {
            if (userStore.conversations.length === 0) {
                await userStore.fetchConversations();
            }
            const conv = userStore.conversations.find(c => String(c.id) === String(conversationId));
            if (conv) {
                otherUser.value = conv.otherUser;
            }
        }
        scrollToBottom();
    } catch (error) {
        console.error('加载聊天数据失败:', error);
    }
};

// 处理从详情页、联系客服、管理员介入进入时的自动消息
const handleAutoMessages = async () => {
    try {
        await loadChatData();
        // 如果已经有聊天记录了，且不是管理员带 orderId 进来的，就不再重复发送自动消息
        if (messages.value.length > 0 && !initialOrderId) return;

        if (initialProductId) {
            const product = await productStore.fetchProductById(initialProductId);
            if (product) {
                await userStore.sendChatMessage(conversationId, '您好，我想咨询一下这个商品：');
                const cardData = {
                    id: product.id,
                    title: product.title,
                    price: product.price,
                    imageUrl: product.imageUrl || `https://picsum.photos/400/300?random=${product.id}`
                };
                await userStore.sendChatMessage(conversationId, `[PRODUCT_CARD]${JSON.stringify(cardData)}`);
            }
        } else if (initialOrderId) {
            // 管理员介入维权订单
            const response = await apiClient.get(`/orders/${initialOrderId}`);
            const orderDetail = response.data;
            if (orderDetail) {
                await userStore.sendChatMessage(conversationId, '您好，我是平台管理员，正在为您处理维权争议。这是相关的订单信息：');
                const orderCardData = {
                    id: orderDetail.order.id,
                    orderNumber: orderDetail.order.orderNumber,
                    title: orderDetail.product.title,
                    price: orderDetail.order.totalPrice,
                    imageUrl: orderDetail.product.imageUrl || `https://picsum.photos/200/200?random=${orderDetail.order.id}`
                };
                await userStore.sendChatMessage(conversationId, `[ORDER_CARD]${JSON.stringify(orderCardData)}`);
            }
        } else if (isFromSupport) {
            await userStore.sendChatMessage(conversationId, '您好，我需要客服支持，请帮我处理一下问题。');
        }
        
        await loadChatData();
    } catch (error) {
        console.error('自动发送消息失败:', error);
    }
}

onMounted(() => {
    userStore.fetchProfile();
    
    if (initialProductId || isFromSupport || initialOrderId) {
        handleAutoMessages();
    } else {
        loadChatData();
    }
    refreshInterval = setInterval(loadChatData, 3000);
});

onUnmounted(() => {
    if (refreshInterval) clearInterval(refreshInterval);
});

const goBack = () => {
    router.back();
};

const handleSendMessage = async () => {
    if (!newMessageContent.value.trim() || sending.value) return;
    
    sending.value = true;
    try {
        await userStore.sendChatMessage(conversationId, newMessageContent.value);
        newMessageContent.value = '';
        await loadChatData();
    } catch (error) {
        ElMessage.error('消息发送失败');
    } finally {
        sending.value = false;
    }
}

</script>

<style scoped>
.chat-window-page {
    max-width: 100%;
}
.custom-input :deep(.el-input__wrapper) {
    border-radius: 24px;
    background-color: #f3f4f6;
    box-shadow: none !important;
}
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}
.pb-safe {
    padding-bottom: env(safe-area-inset-bottom);
}
</style>
