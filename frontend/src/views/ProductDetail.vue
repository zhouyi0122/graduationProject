<template>
  <div class="product-detail-page bg-gray-100 min-h-screen">
    <div v-if="product" class="pb-20"> <!-- Main container for product content -->
      <!-- Back Button -->
      <button @click="router.back()" class="fixed top-4 left-4 z-50 h-10 w-10 bg-black/30 text-white rounded-full flex items-center justify-center">
        <el-icon><ArrowLeftBold /></el-icon>
      </button>

      <!-- Image Carousel -->
      <div class="relative bg-white">
        <el-carousel height="375px" indicator-position="none" arrow="always" :autoplay="false" @change="handleCarouselChange">
          <el-carousel-item v-for="(item, index) in product.images" :key="index">
            <el-image 
              :src="item.src" 
              fit="contain" 
              class="w-full h-full cursor-zoom-in bg-gray-50"
              @click="openPreview(index)"
            />
          </el-carousel-item>
        </el-carousel>
        <!-- 指示器 -->
        <div v-if="product.images.length > 1" class="absolute bottom-4 right-4 bg-black/50 text-white text-[10px] px-2 py-0.5 rounded-full z-10">
          {{ currentImageIndex + 1 }}/{{ product.images.length }}
        </div>
      </div>

      <!-- 大图预览 -->
      <el-image-viewer
        v-if="showViewer"
        :url-list="product.images.map(img => img.src)"
        :initial-index="previewIndex"
        @close="showViewer = false"
      />

      <div class="p-4 space-y-4">
        <!-- Seller Info -->
        <div class="flex items-center justify-between bg-white p-3 rounded-lg shadow-sm">
          <div class="flex items-center">
            <el-avatar :size="40" :src="product.seller.avatarUrl" />
            <div class="ml-3">
              <div class="flex items-center">
                <p class="font-semibold text-gray-800">{{ product.seller.name }}</p>
                <el-tag v-if="product.seller.isCertified" type="warning" size="small" class="ml-2">已认证</el-tag>
              </div>
              <p class="text-xs text-gray-400 mt-0.5">10分钟前来过</p>
            </div>
          </div>
        </div>

        <!-- Price, Title, Description -->
        <div class="bg-white p-3 rounded-lg shadow-sm">
          <div class="flex items-baseline space-x-2">
              <span class="text-3xl font-bold text-red-500">¥{{ product.price }}</span>
              <span class="text-sm text-gray-400 line-through">¥{{ (product.price * 1.2).toFixed(2) }}</span>
          </div>
          <div class="flex items-center text-sm text-gray-400 mt-2 space-x-4">
              <span>{{ favoriteCount }}人收藏</span>
              <span>{{ product.views }}次浏览</span>
          </div>
          <h1 class="mt-4 text-lg font-bold text-gray-900">{{ product.title }}</h1>
          <p class="mt-2 text-sm text-gray-600 leading-relaxed">{{ product.description }}</p>
        </div>

        <!-- Reviews & Comments -->
        <div class="bg-white p-3 rounded-lg shadow-sm">
          <!-- Reviews -->
          <div>
            <div class="flex justify-between items-center">
              <h2 class="text-md font-semibold">服务评价 ({{ product.reviews.length }})</h2>
              <button @click="reviewsExpanded = !reviewsExpanded" class="text-sm text-gray-500 flex items-center">
                全部 <el-icon class="ml-1 transition-transform" :class="{ 'rotate-90': reviewsExpanded }"><ArrowRightBold /></el-icon>
              </button>
            </div>
            <div class="mt-4 space-y-4">
              <div v-for="review in displayedReviews" :key="review.id">
                  <div class="flex items-start">
                      <el-avatar :size="32" :src="review.user.avatarUrl" />
                      <div class="ml-3 flex-grow">
                          <div class="flex justify-between items-center">
                              <p class="text-sm font-medium">{{ review.user.name }}</p>
                              <button v-if="isCurrentUserSeller && !review.reply" @click="() => openReviewReplyDialog(review)" class="text-xs text-gray-500 hover:text-orange-500">回复</button>
                          </div>
                          <p class="text-sm text-gray-700 mt-1">{{ review.comment }}</p>
                      </div>
                  </div>
                  <!-- Seller's Reply -->
                  <div v-if="review.reply" class="ml-10 mt-2 p-2 bg-gray-100 rounded-md">
                      <p class="text-sm"><span class="font-semibold text-orange-600">卖家回复：</span>{{ review.reply }}</p>
                  </div>
              </div>
            </div>
          </div>

          <!-- Divider -->
          <div class="border-t border-dashed border-gray-200 my-4"></div>

          <!-- Comments -->
          <div>
            <div class="flex justify-between items-center">
              <h2 class="text-md font-semibold">留言 ({{ comments.length }})</h2>
              <button @click="commentsExpanded = !commentsExpanded" class="text-sm text-gray-500 flex items-center">
                全部 <el-icon class="ml-1 transition-transform" :class="{ 'rotate-90': commentsExpanded }"><ArrowRightBold /></el-icon>
              </button>
            </div>
            <div class="mt-4 space-y-4">
              <div v-for="comment in displayedComments" :key="comment.id">
                  <div class="flex items-start">
                      <el-avatar :size="32" :src="comment.user.avatarUrl" />
                      <div class="ml-3 flex-grow">
                          <div class="flex justify-between items-center">
                              <p class="text-sm font-medium">{{ comment.user.name }}</p>
                              <button @click="() => openReplyDialog(comment)" class="text-xs text-gray-500 hover:text-orange-500">回复</button>
                          </div>
                          <p class="text-sm text-gray-700 mt-1">{{ comment.comment }}</p>
                      </div>
                  </div>
                  <div v-if="comment.replies && comment.replies.length > 0" class="ml-10 mt-2 p-2 bg-gray-100 rounded-md space-y-2">
                      <div v-for="reply in comment.replies" :key="reply.id" class="flex items-start">
                          <el-avatar :size="28" :src="reply.user.avatarUrl" />
                          <div class="ml-2">
                              <p class="text-sm font-medium">{{ reply.user.name }}</p>
                              <p class="text-sm text-gray-600">{{ reply.comment }}</p>
                          </div>
                      </div>
                  </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Action Bar -->
      <div class="fixed inset-x-0 bottom-0 bg-white border-t border-gray-200 h-16 flex items-center px-4 z-50">
        <!-- Admin View -->
        <div v-if="isAdmin" class="w-full">
          <el-button :type="product.status === '在售' ? 'danger' : 'success'" size="large" class="w-full" @click="handleToggleStatus">
            {{ product.status === '在售' ? '下架商品' : '重新上架' }}
          </el-button>
        </div>
        <!-- User View -->
        <div v-else class="w-full flex items-center">
          <div class="flex items-center space-x-6">
              <button @click="commentDialogVisible = true" class="flex flex-col items-center text-gray-600">
              <el-icon :size="20"><ChatDotRound /></el-icon>
              <span class="text-xs">留言</span>
              </button>
              <button @click="toggleFavorite" class="flex flex-col items-center" :class="isFavorited ? 'text-orange-500' : 'text-gray-600'">
              <component :is="isFavorited ? HeartIconSolid : HeartIconOutline" class="h-6 w-6" />
              <span class="text-xs">收藏</span>
              </button>
          </div>
          <div class="flex-grow flex items-center justify-end space-x-3">
              <el-button size="large" @click="startChat">聊一聊</el-button>
              <el-button type="primary" size="large" @click="handleBuyNow">立即购买</el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="商品不存在或已被删除"></el-empty>

    <!-- Dialogs -->
    <el-dialog v-model="commentDialogVisible" title="发布留言" width="90%">
        <el-input v-model="newComment" type="textarea" :rows="4" placeholder="留下你的留言吧..." />
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="commentDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handlePostComment">发送</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-if="replyingToComment" v-model="replyDialogVisible" :title="`回复 @${replyingToComment.user.name}`" width="90%">
        <el-input v-model="newReply" type="textarea" :rows="4" :placeholder="`回复 @${replyingToComment.user.name}`" />
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="replyDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handlePostReply">发送</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-if="replyingToReview" v-model="reviewReplyDialogVisible" :title="`回复评价`" width="90%">
        <el-input v-model="newReviewReply" type="textarea" :rows="4" :placeholder="`回复 @${replyingToReview.user.name}`" />
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="reviewReplyDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handlePostReviewReply">发送</el-button>
            </span>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import { useAuthStore } from '../stores/auth.store';
import { useProductStore } from '../stores/product.store';
import apiClient from '../services/api';
import { ArrowLeftBold, ArrowRightBold, ChatDotRound, Plus } from '@element-plus/icons-vue';
import { HeartIcon as HeartIconOutline } from '@heroicons/vue/24/outline';
import { HeartIcon as HeartIconSolid } from '@heroicons/vue/24/solid';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const productStore = useProductStore();

const isAdmin = computed(() => authStore.isAdmin);

const product = ref(null);
const loading = ref(true);
const comments = ref([]);

const currentImageIndex = ref(0);
const showViewer = ref(false);
const previewIndex = ref(0);

const handleCarouselChange = (index) => {
    currentImageIndex.value = index;
};

const openPreview = (index) => {
    previewIndex.value = index;
    showViewer.value = true;
};

const fetchProductComments = async () => {
    if (!product.value?.id) return;
    try {
        const response = await apiClient.get(`/products/${product.value.id}/comments`);
        comments.value = response.data.map(c => ({
            ...c,
            user: {
                name: c.user?.nickname || '用户' + c.userId,
                avatarUrl: c.user?.avatar || `https://picsum.photos/100/100?random=u${c.userId}`
            },
            replies: c.replies?.map(r => ({
                ...r,
                user: {
                    name: r.user?.nickname || '用户' + r.userId,
                    avatarUrl: r.user?.avatar || `https://picsum.photos/100/100?random=u${r.userId}`
                },
                comment: r.content // 适配模板字段
            })) || [],
            comment: c.content // 适配模板字段
        }));
    } catch (error) {
        console.error('获取留言失败:', error);
    }
};

const fetchProductDetail = async () => {
    const productId = route.params.id;
    try {
        const response = await apiClient.get(`/products/${productId}`);
        const data = response.data;
        
        // 格式化后端数据以适应模板
        product.value = {
            ...data,
            images: data.images?.length > 0 ? data.images.map(img => ({ src: img.imageUrl, alt: '商品图片' })) : [{ src: data.imageUrl || `https://picsum.photos/800/600?random=${productId}`, alt: '商品图片' }],
            seller: {
                id: data.sellerId,
                name: data.seller?.nickname || '未知卖家',
                avatarUrl: data.seller?.avatar || `https://picsum.photos/150/150?random=${data.sellerId || 'default'}`,
                isCertified: data.seller?.isCertified === 1
            },
            reviews: [], // 暂时留空，待评论模块联调
            isFavorited: data.isFavorited || false,
            favoriteCount: data.favoriteCount || 0
        };
        // 同步组件内部状态
        isFavorited.value = product.value.isFavorited;
        favoriteCountState.value = product.value.favoriteCount;

        // 获取留言
        await fetchProductComments();
    } catch (error) {
        console.error('获取商品详情失败:', error);
        ElMessage.error('商品不存在或已被删除');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchProductDetail();
});

const isFavorited = ref(false);
const favoriteCountState = ref(0);
const commentDialogVisible = ref(false);
const newComment = ref('');
const reviewsExpanded = ref(false);
const commentsExpanded = ref(false);

const replyDialogVisible = ref(false);
const newReply = ref('');
const replyingToComment = ref(null);

const reviewReplyDialogVisible = ref(false);
const newReviewReply = ref('');
const replyingToReview = ref(null);

const isCurrentUserSeller = computed(() => userStore.profile?.username === product.value?.seller?.username);
const favoriteCount = computed(() => favoriteCountState.value);
const displayedReviews = computed(() => reviewsExpanded.value ? product.value?.reviews : product.value?.reviews?.slice(0, 1));
const displayedComments = computed(() => commentsExpanded.value ? comments.value : comments.value?.slice(0, 1));

const toggleFavorite = async () => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再进行操作');
        router.push('/login');
        return;
    }
    try {
        const res = await productStore.toggleFavorite(product.value.id);
        const { status } = res;
        isFavorited.value = (status === 1);
        // 更新收藏总数
        if (status === 1) {
            favoriteCountState.value++;
        } else {
            favoriteCountState.value--;
        }
        ElMessage.success(res.message);
    } catch (error) {
        console.error('切换收藏状态失败:', error);
        ElMessage.error('操作失败，请检查网络或登录状态');
    }
};

const startChat = async () => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再进行操作');
        router.push('/login');
        return;
    }
    
    // 确保不跟自己聊天
    if (String(product.value.sellerId) === String(authStore.user.id)) {
        ElMessage.warning('这是您自己发布的商品哦');
        return;
    }

    try {
        // 调用 store 中真实的接口，根据卖家 ID 获取或创建会话
        const conv = await userStore.getOrCreateConversation(product.value.sellerId);
        if (conv && conv.id) {
            // 跳转时带上 productId，以便聊天窗识别并发送商品卡片
            router.push({
                path: `/chat/${conv.id}`,
                query: { productId: product.value.id }
            });
        }
    } catch (error) {
        console.error('发起聊天失败:', error);
        ElMessage.error('无法发起聊天，请稍后再试');
    }
}

const handleBuyNow = () => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再进行操作');
        router.push('/login');
        return;
    }
    
    // 校验：不能购买自己发布的商品
    if (String(product.value.sellerId) === String(authStore.user.id)) {
        ElMessage.warning('这是您自己发布的商品哦');
        return;
    }

    router.push({
        name: 'OrderConfirmation',
        query: { product: JSON.stringify(product.value) }
    });
}

const handleToggleStatus = () => {
    const actionText = product.value.status === '在售' ? '下架' : '上架';
    ElMessageBox.confirm(`您确定要${actionText}该商品吗？`, '确认操作', { type: 'warning' })
    .then(() => {
        userStore.adminToggleProductStatus(product.value.id);
        ElMessage.success(`${actionText}成功`);
    }).catch(() => {});
}

const handlePostComment = async () => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再发表留言');
        router.push('/login');
        return;
    }
    if (!newComment.value.trim()) {
        ElMessage.warning('留言内容不能为空！');
        return;
    }
    try {
        await apiClient.post(`/products/${product.value.id}/comments`, { content: newComment.value });
        ElMessage.success('留言成功！');
        commentDialogVisible.value = false;
        newComment.value = '';
        await fetchProductComments(); // 重新拉取留言列表
    } catch (error) {
        console.error('发表留言失败:', error);
        ElMessage.error('留言失败，请检查登录状态');
    }
};

const openReplyDialog = (comment) => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再回复');
        router.push('/login');
        return;
    }
    replyingToComment.value = comment;
    replyDialogVisible.value = true;
}

const handlePostReply = async () => {
    if (!newReply.value.trim()) {
        ElMessage.warning('回复内容不能为空！');
        return;
    }
    try {
        await apiClient.post(`/products/comments/${replyingToComment.value.id}/replies`, { content: newReply.value });
        ElMessage.success('回复成功！');
        replyDialogVisible.value = false;
        newReply.value = '';
        await fetchProductComments(); // 重新拉取留言列表
    } catch (error) {
        console.error('回复失败:', error);
        ElMessage.error('回复失败，请稍后再试');
    }
}

const openReviewReplyDialog = (review) => {
    replyingToReview.value = review;
    reviewReplyDialogVisible.value = true;
}

const handlePostReviewReply = () => {
    if (!newReviewReply.value.trim()) {
        ElMessage.warning('回复内容不能为空！');
        return;
    }
    console.log(`【模拟回复评价】@${replyingToReview.value.user.name}:`, newReviewReply.value);
    reviewReplyDialogVisible.value = false;
    newReviewReply.value = '';
}

</script>
