<template>
  <div class="my-favorites-page bg-gray-100 min-h-screen" :class="{ 'pb-20': isManaging }">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-4 py-2 flex items-center relative h-14">
        <button @click="handleBack" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">我的收藏</h1>
        <button @click="toggleManagement" class="absolute right-2 p-2 text-sm font-semibold" :class="isManaging ? 'text-orange-500' : 'text-gray-600'">
          {{ isManaging ? '完成' : '管理' }}
        </button>
      </div>
    </header>

    <!-- Filter Tabs -->
    <div class="bg-white border-b border-gray-200">
        <el-tabs v-model="activeFilter" class="container mx-auto custom-tabs" stretch>
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="有效宝贝" name="valid"></el-tab-pane>
          <el-tab-pane label="失效宝贝" name="invalid"></el-tab-pane>
        </el-tabs>
    </div>

    <div class="p-4">
      <div v-if="filteredFavorites.length > 0" class="space-y-3">
        <div v-for="product in filteredFavorites" :key="product.id" 
             class="bg-white p-3 rounded-lg shadow-sm flex space-x-4 items-center transition-all cursor-pointer"
             :class="{ 'opacity-60': product.status === 2, 'pl-12': isManaging }"
             @click="() => handleItemClick(product)">
          
          <div v-if="isManaging" class="absolute left-8">
            <el-checkbox :model-value="selectedItems.has(product.id)" size="large" @change="() => toggleSelection(product.id)" @click.stop />
          </div>

          <div class="relative w-24 h-24 rounded-md overflow-hidden flex-shrink-0">
            <img :src="product.imageUrl" :alt="product.title" class="w-full h-full object-cover" />
            <div v-if="product.status === 2" class="absolute inset-0 bg-black/50 flex items-center justify-center">
                <span class="text-white text-sm font-bold">已下架</span>
            </div>
          </div>
          <div class="flex-grow min-w-0 flex flex-col space-y-1">
            <h3 class="text-base font-medium text-gray-800 leading-tight line-clamp-2" :class="{ 'text-gray-400': product.status === 2 }">{{ product.title }}</h3>
            <p class="text-lg font-bold" :class="product.status === 2 ? 'text-gray-400' : 'text-red-500'">¥{{ product.price }}</p>
            <div class="flex items-center text-xs text-gray-400">
                <img class="h-5 w-5 rounded-full object-cover mr-1.5" :src="product.seller.avatarUrl" alt="seller avatar"/>
                <span>{{ product.seller.nickname }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else :description="emptyDescription"></el-empty>
    </div>

    <!-- Bottom Management Bar -->
    <div v-if="isManaging" class="fixed inset-x-0 bottom-0 bg-white border-t border-gray-200 h-16 flex items-center justify-between px-4 z-50">
        <el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleSelectAll">全选</el-checkbox>
        <div class="flex items-center space-x-2">
            <el-button round plain @click="clearInvalid">清空失效</el-button>
            <el-button round type="primary" @click="deleteSelected" :class="{ 'opacity-50 cursor-not-allowed': selectedItems.size === 0 }">删除 ({{ selectedItems.size }})</el-button>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const isManaging = ref(false);
const selectedItems = ref(new Set());
const activeFilter = ref('all'); // 'all', 'valid', 'invalid'

const filteredFavorites = computed(() => {
    switch (activeFilter.value) {
        case 'valid':
            return userStore.favoriteProducts.filter(p => p.status !== 2);
        case 'invalid':
            return userStore.favoriteProducts.filter(p => p.status === 2);
        default:
            return userStore.favoriteProducts;
    }
});

const emptyDescription = computed(() => {
    switch (activeFilter.value) {
        case 'valid': return '没有有效的收藏';
        case 'invalid': return '没有失效的收藏';
        default: return '您还没有收藏任何商品';
    }
});

const isAllSelected = computed(() => {
    return filteredFavorites.value.length > 0 && selectedItems.value.size === filteredFavorites.value.length;
});

const isIndeterminate = computed(() => {
    return selectedItems.value.size > 0 && selectedItems.value.size < filteredFavorites.value.length;
});

const handleBack = () => {
    if (isManaging.value) {
        isManaging.value = false;
        selectedItems.value.clear();
    } else {
        router.back();
    }
}

const handleItemClick = (product) => {
    if (isManaging.value) {
        toggleSelection(product.id);
    } else if (product.status !== 2) {
        router.push(`/product/${product.id}`);
    }
}

const toggleManagement = () => {
    isManaging.value = !isManaging.value;
    selectedItems.value.clear();
}

const toggleSelection = (id) => {
    if (selectedItems.value.has(id)) {
        selectedItems.value.delete(id);
    } else {
        selectedItems.value.add(id);
    }
}

const toggleSelectAll = (value) => {
    if (value) {
        filteredFavorites.value.forEach(p => selectedItems.value.add(p.id));
    } else {
        selectedItems.value.clear();
    }
}

const deleteSelected = () => {
    if (selectedItems.value.size === 0) return;
    ElMessageBox.confirm(`您确定要删除选中的 ${selectedItems.value.size} 个收藏吗?`, '确认删除', { type: 'warning' })
    .then(() => {
        userStore.deleteFavoriteProducts([...selectedItems.value]);
        ElMessage.success('删除成功');
        selectedItems.value.clear();
    }).catch(() => {});
}

const clearInvalid = () => {
    const invalidItems = userStore.favoriteProducts.filter(p => p.status === 2);
    if (invalidItems.length === 0) {
        ElMessage.info('没有已失效的宝贝');
        return;
    }
    ElMessageBox.confirm(`您确定要清空 ${invalidItems.length} 个已失效的宝贝吗?`, '确认清空', { type: 'warning' })
    .then(() => {
        userStore.clearInvalidFavorites();
        ElMessage.success('已清空失效宝贝');
    }).catch(() => {});
}

</script>

<style scoped>
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

:deep(.el-checkbox__inner) {
    border-radius: 50%;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner),
:deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background-color: #f97316 !important;
  border-color: #f97316 !important;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: #f97316 !important;
}

.custom-tabs :deep(.el-tabs__nav) {
    float: none;
}
.custom-tabs :deep(.el-tabs__item) {
    padding: 0 20px;
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
</style>
