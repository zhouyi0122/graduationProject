<template>
  <router-link :to="`/product/${product.id}`" class="group flex flex-col overflow-hidden rounded-lg bg-white shadow-sm transition-shadow duration-300 hover:shadow-lg">
    <!-- Image area -->
    <img :src="product.imageUrl" :alt="product.title" class="h-auto w-full object-cover" />
    
    <!-- Text area with flex layout -->
    <div class="p-2 flex flex-col flex-grow space-y-1">
      <!-- Title (no fixed height) -->
      <h3 class="text-sm font-medium text-gray-800 leading-tight line-clamp-2">{{ product.title }}</h3>
      
      <!-- Spacer to push content to the bottom -->
      <div class="flex-grow"></div>

      <!-- Price and Stats -->
      <div class="flex items-center justify-between">
        <p class="text-base font-bold text-red-500">¥{{ product.price }}</p>
        <div class="flex items-center text-xs text-gray-400 space-x-2">
            <span class="inline-flex items-center">
                <el-icon :size="14" class="mr-0.5"><View /></el-icon>
                <span>{{ product.views ?? 0 }}</span>
            </span>
            <span class="inline-flex items-center cursor-pointer hover:text-orange-500" @click.prevent.stop="toggleFavorite">
                <component :is="isFavorited ? HeartIconSolid : HeartIconOutline" class="h-4 w-4 mr-0.5" :class="{ 'text-orange-500': isFavorited }" />
                <span>{{ localFavoriteCount }}</span>
            </span>
        </div>
      </div>

      <!-- Seller Info -->
      <div class="flex items-center border-t border-gray-100 pt-1.5">
        <img class="h-5 w-5 rounded-full object-cover" :src="product.seller?.avatar || `https://picsum.photos/40?random=${product.id}`" alt="seller avatar"/>
        <p class="ml-1.5 text-xs text-gray-500 truncate">{{ product.seller?.nickname || '未知卖家' }}</p>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { ref, watch } from 'vue';
import { View } from '@element-plus/icons-vue';
import { HeartIcon as HeartIconOutline } from '@heroicons/vue/24/outline';
import { HeartIcon as HeartIconSolid } from '@heroicons/vue/24/solid';
import { useProductStore } from '../stores/product.store';
import { useAuthStore } from '../stores/auth.store';
import { ElMessage } from 'element-plus';

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const productStore = useProductStore();
const authStore = useAuthStore();

const isFavorited = ref(props.product.isFavorited || false);
const localFavoriteCount = ref(props.product.favoriteCount || 0);

// 监听 props 变化，确保详情页修改后回到首页能同步状态
watch(() => props.product.isFavorited, (newVal) => {
    isFavorited.value = newVal;
});
watch(() => props.product.favoriteCount, (newVal) => {
    localFavoriteCount.value = newVal;
});

const toggleFavorite = async () => {
    if (!authStore.isLoggedIn) {
        ElMessage.warning('请先登录后再进行操作');
        return;
    }
    try {
        const res = await productStore.toggleFavorite(props.product.id);
        // 移除这里的手动加减逻辑，完全交给 productStore 的数据和 watch 监听器
        ElMessage.success(res.message);
    } catch (error) {
        console.error('切换收藏状态失败:', error);
        ElMessage.error('操作失败，请检查网络或登录状态');
    }
};

</script>

<style scoped>
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}
</style>