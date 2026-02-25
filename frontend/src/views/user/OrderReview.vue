<template>
  <div class="order-review-page bg-gray-100 min-h-screen pb-20">
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">评价详情</h1>
      </div>
    </header>

    <div class="p-4">
      <el-card v-loading="loading" class="review-card shadow-md">
        <template #header>
          <div class="flex justify-between items-center">
            <div class="flex items-center">
              <el-icon class="text-orange-500 mr-2"><StarFilled /></el-icon>
              <span class="font-bold">交易评价</span>
            </div>
            <el-tag v-if="order" type="info" size="small">订单号: {{ order.orderNumber || order.order_no }}</el-tag>
          </div>
        </template>

        <!-- 评价列表 -->
        <div v-if="rootReviews.length > 0" class="space-y-8">
          <div v-for="review in rootReviews" :key="review.id" class="review-item">
            <div class="flex items-start">
              <el-avatar :size="44" :src="review.reviewer?.avatar || `https://picsum.photos/100/100?random=u${review.reviewerId}`" />
              <div class="ml-4 flex-grow">
                <div class="flex justify-between items-center">
                  <span class="font-bold text-gray-800">
                    {{ review.reviewer?.nickname || '匿名用户' }}
                    <el-tag v-if="review.isAdditional === 1" size="small" type="warning" class="ml-2" effect="dark">追评</el-tag>
                  </span>
                  <el-rate v-if="review.isAdditional === 0" v-model="review.rating" disabled />
        </div>
                
                <!-- 买家评价样式 -->
                <p class="text-gray-700 mt-3 text-sm leading-relaxed bg-gray-50 p-3 rounded-lg border border-gray-100">
                  {{ review.content }}
                </p>
        
                <div class="flex justify-between items-center mt-3">
                  <span class="text-xs text-gray-400">{{ formatDate(review.createTime) }}</span>
                  <!-- 回复按钮逻辑 -->
                  <el-button v-if="isSeller && !hasReplied(review.id)" 
                             type="primary" size="small" plain
                             @click="openReplyDialog(review)">
                    回复客户
                  </el-button>
        </div>

                <!-- 卖家回复展示区域：样式与买家一致，色调改为橙色 -->
                <div v-for="reply in getReplies(review.id)" :key="reply.id" class="mt-4">
                  <div class="flex justify-between items-center mb-1 px-1">
                    <span class="text-xs font-bold text-orange-600 flex items-center">
                      <el-icon class="mr-1"><ChatSquare /></el-icon>卖家回复
                    </span>
                    <span class="text-[10px] text-gray-400">{{ formatDate(reply.createTime) }}</span>
                  </div>
                  <p class="text-gray-700 text-sm leading-relaxed bg-orange-50 p-3 rounded-lg border border-orange-100">
                    {{ reply.content }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无评价内容，交易愉快～" />

        <!-- 底部追加评价按钮 -->
        <div v-if="!loading && !isSeller && !hasAdditionalReview && rootReviews.length > 0" 
             class="mt-10 pt-6 border-t border-dashed text-center">
          <el-button type="warning" size="large" @click="additionalReviewDialogVisible = true" class="px-8">
            追加评价
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 弹窗部分 -->
    <el-dialog v-model="additionalReviewDialogVisible" title="我的追评" width="90%" custom-class="custom-dialog">
      <el-input v-model="additionalForm.content" type="textarea" :rows="5" placeholder="分享您的后续使用感受..." />
      <template #footer>
        <div class="flex space-x-3">
          <el-button @click="additionalReviewDialogVisible = false" class="flex-grow">取消</el-button>
          <el-button type="primary" @click="submitAdditionalReview" class="flex-grow">提交追评</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" title="回复买家评价" width="90%" custom-class="custom-dialog">
      <el-input v-model="replyForm.content" type="textarea" :rows="5" placeholder="感谢您的支持，我们会做得更好..." />
      <template #footer>
        <div class="flex space-x-3">
          <el-button @click="replyDialogVisible = false" class="flex-grow">取消</el-button>
          <el-button type="primary" @click="submitReply" class="flex-grow">发送回复</el-button>
    </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth.store';
import apiClient from '../../services/api';
import { ArrowLeftBold, StarFilled, ChatSquare } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const orderId = route.params.id;
const order = ref(null);
const reviews = ref([]);
const loading = ref(true);

const additionalReviewDialogVisible = ref(false);
const additionalForm = reactive({ content: '' });

const replyDialogVisible = ref(false);
const replyForm = reactive({ content: '', parentId: null });

// 身份判断
const isSeller = computed(() => {
    if (!order.value || !authStore.user) return false;
    return String(order.value.sellerId) === String(authStore.user.id);
});

const rootReviews = computed(() => reviews.value.filter(r => !r.parentId || r.parentId === 0 || r.parentId === '0'));
const getReplies = (parentId) => reviews.value.filter(r => String(r.parentId) === String(parentId));
const hasAdditionalReview = computed(() => reviews.value.some(r => r.isAdditional === 1));
const hasReplied = (parentId) => reviews.value.some(r => String(r.parentId) === String(parentId));

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const fetchData = async () => {
  loading.value = true;
  try {
    const [orderRes, reviewRes] = await Promise.all([
      apiClient.get(`/orders/${orderId}`),
      apiClient.get(`/orders/${orderId}/reviews`)
    ]);
    order.value = orderRes.data.order || orderRes.data;
    reviews.value = reviewRes.data;
  } catch (error) {
    console.error('加载详情失败:', error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
    }
};

const submitAdditionalReview = async () => {
  if (!additionalForm.content.trim()) return ElMessage.warning('请输入内容');
  try {
    await apiClient.post(`/orders/${orderId}/review`, {
      comment: additionalForm.content,
      rating: 5,
      isAdditional: 1
    });
    ElMessage.success('追评已发布');
    additionalReviewDialogVisible.value = false;
    additionalForm.content = '';
    fetchData();
  } catch (error) { ElMessage.error('发布失败'); }
};

const openReplyDialog = (review) => {
  replyForm.parentId = review.id;
  replyForm.content = '';
  replyDialogVisible.value = true;
};

const submitReply = async () => {
  if (!replyForm.content.trim()) return ElMessage.warning('请输入回复内容');
  try {
    await apiClient.post(`/orders/${orderId}/review`, {
      comment: replyForm.content,
      parentId: replyForm.parentId,
      targetType: 'buyer'
    });
    ElMessage.success('回复成功');
    replyDialogVisible.value = false;
    fetchData();
  } catch (error) { ElMessage.error('回复失败'); }
};

onMounted(fetchData);
</script>

<style scoped>
.review-card {
  border-radius: 16px;
  overflow: hidden;
}
.review-item {
  padding-bottom: 24px;
  border-bottom: 1px dashed #eee;
}
.review-item:last-child {
  border-bottom: none;
}
:deep(.el-card__header) {
  background-color: #fafafa;
  padding: 12px 20px;
}
:deep(.el-rate) {
  height: auto;
}
.custom-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>
