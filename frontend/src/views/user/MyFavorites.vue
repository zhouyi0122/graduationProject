<template>
  <div class="my-favorites-page bg-gray-100 min-h-screen pb-20">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">我的收藏</h1>
        <button v-if="userStore.favoriteProducts.length > 0" 
                @click="isEditMode = !isEditMode" 
                class="absolute right-2 p-2 text-gray-600 font-medium">
          {{ isEditMode ? '完成' : '管理' }}
        </button>
      </div>
    </header>

    <div class="p-4" v-loading="loading">
      <div v-if="userStore.favoriteProducts.length > 0" class="space-y-4">
        <div v-for="product in userStore.favoriteProducts" :key="product.id" 
             class="bg-white rounded-xl shadow-sm overflow-hidden flex items-center p-3 relative transition-all"
             :class="{ 'opacity-60 grayscale': product.status !== 0, 'cursor-pointer': product.status === 0 }"
             @click="viewDetail(product)">
          
          <!-- 多选框 (管理模式下显示) -->
          <el-checkbox v-if="isEditMode" 
                       v-model="selectedIds" 
                       :label="product.id" 
                       @click.stop 
                       class="mr-3" />

          <!-- 商品图片 -->
          <img :src="product.imageUrl || `https://picsum.photos/200/200?random=${product.id}`" 
               class="w-24 h-24 rounded-lg object-cover flex-shrink-0" />

          <!-- 商品信息 -->
          <div class="ml-3 flex-grow min-w-0 flex flex-col justify-between h-24">
            <div>
              <h3 class="text-sm font-bold text-gray-800 line-clamp-2">{{ product.title }}</h3>
              <p v-if="product.status !== 0" class="text-[10px] text-red-500 mt-1">
                {{ product.status === 1 ? '商品已售出' : '商品已下架' }}
              </p>
            </div>
            <div class="flex justify-between items-baseline">
              <span class="text-lg font-bold text-red-500">¥{{ product.price }}</span>
              <span class="text-[10px] text-gray-400">已收藏</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="还没有收藏任何宝贝哦~" :image-size="200">
        <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <!-- 底部删除工具栏 -->
    <footer v-if="isEditMode && userStore.favoriteProducts.length > 0" 
            class="fixed bottom-0 inset-x-0 bg-white border-t border-gray-200 h-16 flex items-center justify-between px-4 z-50">
      <el-checkbox :model-value="isAllSelected" @change="toggleSelectAll">全选</el-checkbox>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
        取消收藏 ({{ selectedIds.length }})
      </el-button>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import apiClient from '../../services/api';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const isEditMode = ref(false);
const selectedIds = ref([]);

const isAllSelected = computed(() => {
  return selectedIds.value.length === userStore.favoriteProducts.length && userStore.favoriteProducts.length > 0;
});

const toggleSelectAll = (val) => {
  selectedIds.value = val ? userStore.favoriteProducts.map(p => p.id) : [];
};

const fetchFavorites = async () => {
  loading.value = true;
  try {
    await userStore.fetchMyFavorites();
  } catch (error) {
    ElMessage.error('加载收藏列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchFavorites);

const viewDetail = (product) => {
  if (isEditMode.value) return;
  if (product.status !== 0) {
    ElMessage.info('该商品已下架或售出，无法查看详情');
    return;
  }
  router.push(`/product/${product.id}`);
};

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要取消收藏这 ${selectedIds.value.length} 件宝贝吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 由于后端目前是切换接口，循环调用实现批量取消
      for (const id of selectedIds.value) {
        await apiClient.post(`/products/${id}/favorite`);
      }
      ElMessage.success('已取消收藏');
      selectedIds.value = [];
      isEditMode.value = false;
      fetchFavorites();
    } catch (error) {
      ElMessage.error('操作失败，请重试');
    }
  }).catch(() => {});
};
</script>

<style scoped>
.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #f97316;
  border-color: #f97316;
}
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #f97316;
}
</style>
