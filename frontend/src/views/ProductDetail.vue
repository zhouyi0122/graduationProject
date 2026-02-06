<template>
  <div class="product-detail-page bg-gray-100 min-h-screen">
    <div v-if="product" class="pb-20"> <!-- Main container for product content -->
      <!-- Back Button -->
      <button @click="router.back()" class="fixed top-4 left-4 z-50 h-10 w-10 bg-black/30 text-white rounded-full flex items-center justify-center">
        <el-icon><ArrowLeftBold /></el-icon>
      </button>

      <!-- Image Carousel -->
      <el-carousel height="350px" indicator-position="outside">
        <el-carousel-item v-for="item in product.images" :key="item.src">
          <img :src="item.src" :alt="item.alt" class="w-full h-full object-cover" />
        </el-carousel-item>
      </el-carousel>

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
              <h2 class="text-md font-semibold">留言 ({{ product.comments.length }})</h2>
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
import { ArrowLeftBold, ArrowRightBold, ChatDotRound, Plus } from '@element-plus/icons-vue';
import { HeartIcon as HeartIconOutline } from '@heroicons/vue/24/outline';
import { HeartIcon as HeartIconSolid } from '@heroicons/vue/24/solid';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();

const isAdmin = computed(() => authStore.isAdmin);

const product = computed(() => {
    const productId = parseInt(route.params.id, 10);
    
    // Find from allProducts first
    let foundProduct = userStore.allProducts.find(p => p.id === productId);
    
    // Fallback for user's own products
    if (!foundProduct) {
        foundProduct = userStore.myProducts.find(p => p.id === productId);
    }

    if (foundProduct) {
        const sellerName = foundProduct.seller || userStore.profile.nickname;
        const imageUrl = foundProduct.imageUrl || `https://picsum.photos/800/600?random=${productId}`;
        // This is a simplified version. In a real app, you'd fetch full details.
        return {
            ...foundProduct,
            images: foundProduct.images || [{ src: imageUrl, alt: 'Product view' }],
            seller: foundProduct.sellerObject || { id: 123, name: sellerName, avatarUrl: `https://i.pravatar.cc/40?u=${sellerName}`, isCertified: true },
            description: foundProduct.description || '这是一个商品的详细描述...',
            reviews: foundProduct.reviews || [],
            comments: foundProduct.comments || [],
            views: foundProduct.views || 1024,
        };
    }
    
    return null;
});

const isFavorited = ref(false);
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

const isCurrentUserSeller = computed(() => userStore.profile.username === product.value?.seller.name);
const favoriteCount = computed(() => product.value?.favorites || 0);
const displayedReviews = computed(() => reviewsExpanded.value ? product.value?.reviews : product.value?.reviews.slice(0, 1));
const displayedComments = computed(() => commentsExpanded.value ? product.value?.comments : product.value?.comments.slice(0, 1));

const toggleFavorite = () => {
    isFavorited.value = !isFavorited.value;
};

const startChat = () => {
    const conversationId = userStore.getOrCreateConversationBySeller(product.value.seller, product.value.title);
    router.push(`/chat/${conversationId}`);
}

const handleBuyNow = () => {
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

const handlePostComment = () => {
    if (!newComment.value.trim()) {
        ElMessage.warning('留言内容不能为空！');
        return;
    }
    userStore.addCommentToProduct(product.value.id, newComment.value);
    ElMessage.success('留言成功！');
    commentDialogVisible.value = false;
    newComment.value = '';
};

const openReplyDialog = (comment) => {
    replyingToComment.value = comment;
    replyDialogVisible.value = true;
}

const handlePostReply = () => {
    if (!newReply.value.trim()) {
        ElMessage.warning('回复内容不能为空！');
        return;
    }
    userStore.addReplyToComment(product.value.id, replyingToComment.value.id, newReply.value);
    replyDialogVisible.value = false;
    newReply.value = '';
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
