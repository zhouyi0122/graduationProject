<template>
  <div class="product-list-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center space-x-2">
        <button @click="goBack" class="p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <el-input
          v-model="searchQuery"
          placeholder="搜索你想要的宝贝"
          class="flex-grow custom-search-input"
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </header>

    <!-- Filter/Sort Bar -->
    <div class="bg-white border-b border-gray-200">
        <div class="container mx-auto flex justify-around text-sm text-gray-600">
            <button v-for="sort in sortOptions" :key="sort.key" 
                    @click="selectSort(sort.key)"
                    class="py-3 px-4 font-semibold flex items-center"
                    :class="{ 'text-orange-500': activeSort === sort.key }">
                <span>{{ sort.label }}</span>
                <span v-if="sort.key === 'price'" class="ml-1 flex flex-col -space-y-1.5 text-gray-400">
                    <el-icon :size="12" :class="{ 'text-orange-500': activeSort === 'price' && priceSortOrder === 'asc' }"><ArrowUpBold /></el-icon>
                    <el-icon :size="12" :class="{ 'text-orange-500': activeSort === 'price' && priceSortOrder === 'desc' }"><ArrowDownBold /></el-icon>
                </span>
            </button>
        </div>
    </div>

    <!-- Product List -->
    <div class="container mx-auto p-2 space-y-2">
        <div v-for="product in productStore.products" :key="product.id" 
             class="bg-white p-3 rounded-lg shadow-sm flex space-x-4 cursor-pointer h-32"
             @click="() => handleItemClick(product)">
            <img :src="product.imageUrl" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0 self-center" />
            <div class="flex flex-col flex-grow min-w-0 justify-between">
                                <h3 class="text-lg font-medium text-gray-800 leading-tight line-clamp-2">{{ product.title }}</h3>
                <div>
                    <p class="text-lg font-bold text-red-500 mb-1">¥{{ product.price }}</p>
                    <div class="flex items-center justify-between text-xs text-gray-400">
                        <span>{{ product.seller.nickname }}</span>
                        <div class="flex items-center space-x-3">
                            <span class="inline-flex items-center">
                                <el-icon :size="14" class="mr-0.5"><View /></el-icon>
                                <span>{{ Math.floor(Math.random() * 1000) }}</span>
                            </span>
                            <FavoriteButton :initialCount="Math.floor(Math.random() * 200)" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from '../stores/product.store';
import { ArrowLeftBold, Search, ArrowUpBold, ArrowDownBold, View } from '@element-plus/icons-vue';
import FavoriteButton from '../components/FavoriteButton.vue';

const route = useRoute();
const router = useRouter();
const productStore = useProductStore();

const searchQuery = ref('');
const activeSort = ref('comprehensive');
const priceSortOrder = ref('none'); // 'asc', 'desc', 'none'

const sortOptions = ref([
    { key: 'comprehensive', label: '综合' },
    { key: 'latest', label: '最新' },
    { key: 'price', label: '价格' },
    { key: 'credit', label: '收藏' },
]);

onMounted(() => {
  searchQuery.value = route.query.q || '';
});

const goBack = () => {
  router.push('/');
};

const handleItemClick = (product) => {
    router.push(`/product/${product.id}`);
}

const handleSearch = () => {
  console.log('Searching for:', searchQuery.value);
};

const selectSort = (key) => {
    if (key === 'price') {
        if (activeSort.value === 'price') {
            priceSortOrder.value = priceSortOrder.value === 'asc' ? 'desc' : 'asc';
        } else {
            activeSort.value = 'price';
            priceSortOrder.value = 'asc';
        }
    } else {
        activeSort.value = key;
        priceSortOrder.value = 'none';
    }
    console.log('Sorting by:', activeSort.value, priceSortOrder.value !== 'none' ? priceSortOrder.value : '');
}

</script>

<style scoped>
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

:deep(.custom-search-input .el-input__wrapper) {
    border-radius: 20px 0 0 20px !important;
    background-color: #f3f4f6 !important;
    box-shadow: none !important;
    border: none !important;
}

:deep(.custom-search-input .el-input-group__append) {
    background-color: #f3f4f6 !important;
    border-radius: 0 20px 20px 0 !important;
    padding: 0;
    border: none;
}

:deep(.custom-search-input .el-input-group__append .el-button) {
    border-radius: 0 20px 20px 0 !important;
    background-color: #f3f4f6;
    border: none;
    color: #6b7280;
}

:deep(.custom-search-input .el-input-group__append .el-button:hover) {
    background-color: #e5e7eb;
}
</style>
