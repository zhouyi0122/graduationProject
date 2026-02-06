<template>
  <div class="order-review-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">评价详情</h1>
      </div>
    </header>

    <div v-if="order && (order.buyerReview || order.sellerReview)" class="p-4 space-y-4">
      <!-- Buyer Review -->
      <el-card v-if="order.buyerReview">
        <div class="flex items-center mb-4">
          <el-avatar :size="48" :src="isSellerView ? order.buyer.avatarUrl : userStore.profile.avatarUrl || 'https://i.pravatar.cc/40?u=0'" />
          <div class="ml-4">
            <p class="font-semibold">{{ isSellerView ? order.buyer.name : userStore.profile.nickname }}</p>
            <el-rate v-model="order.buyerReview.initial.rating" disabled :colors="['#FF9900', '#FF9900', '#FF9900']" />
          </div>
        </div>
        <p class="text-gray-700">{{ order.buyerReview.initial.comment }}</p>
        
        <div v-if="order.buyerReview.additional" class="mt-4 pt-4 border-t border-dashed">
            <p class="text-sm text-gray-700"><span class="font-semibold text-orange-500">[追评]</span> {{ order.buyerReview.additional.comment }}</p>
            <p class="text-xs text-gray-400 text-right mt-2">{{ order.buyerReview.additional.time }}</p>
        </div>
      </el-card>

      <!-- Seller Review -->
      <el-card v-if="order.sellerReview">
        <div class="flex items-center mb-4">
          <el-avatar :size="48" :src="isSellerView ? userStore.profile.avatarUrl || 'https://i.pravatar.cc/40?u=0' : order.seller.avatarUrl" />
          <div class="ml-4">
            <p class="font-semibold">{{ isSellerView ? userStore.profile.nickname : order.seller.name }} <el-tag size="small" type="warning" class="ml-1">卖家</el-tag></p>
          </div>
        </div>
        <p class="text-gray-700">{{ order.sellerReview.initial.comment }}</p>

        <div v-if="order.sellerReview.additional" class="mt-4 pt-4 border-t border-dashed">
            <p class="text-sm text-gray-700"><span class="font-semibold text-orange-500">[追评]</span> {{ order.sellerReview.additional.comment }}</p>
            <p class="text-xs text-gray-400 text-right mt-2">{{ order.sellerReview.additional.time }}</p>
        </div>
      </el-card>

      <!-- Add Additional Review -->
      <el-card v-if="canAddAdditionalReview">
        <h3 class="font-semibold text-gray-800 mb-2">追加评价</h3>
        <el-input v-model="additionalComment" type="textarea" :rows="4" placeholder="购买之后有什么新发现？快来分享吧～"></el-input>
        <div class="text-right mt-4">
            <el-button type="primary" @click="submitAdditionalReview">提交追评</el-button>
        </div>
      </el-card>

    </div>
    <el-empty v-else description="评价不存在"></el-empty>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const additionalComment = ref('');

const order = computed(() => {
    const orderId = route.params.id;
    const allOrders = [...userStore.boughtOrders, ...userStore.soldOrders];
    return allOrders.find(o => o.id === orderId) || null;
});

const isSellerView = computed(() => {
    if (!order.value) return false;
    // Check if the order is in soldOrders to determine if the current user is the seller
    return userStore.soldOrders.some(o => o.id === order.value.id);
});

const canAddAdditionalReview = computed(() => {
    if (!order.value) return false;
    if (isSellerView.value) {
        return order.value.sellerReview && !order.value.sellerReview.additional;
    } else {
        return order.value.buyerReview && !order.value.buyerReview.additional;
    }
});

const submitAdditionalReview = () => {
    if (!additionalComment.value.trim()) {
        ElMessage.warning('请输入追评内容');
        return;
    }
    const userType = isSellerView.value ? 'seller' : 'buyer';
    userStore.addAdditionalReview(order.value.id, additionalComment.value, userType);
    ElMessage.success('追评成功');
    additionalComment.value = '';
}

</script>
