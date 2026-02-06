<template>
  <div class="pb-16 bg-gray-100"> <!-- 为底部导航栏留出空间 -->
    <!-- 顶部固定搜索栏 -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-4 py-3 flex items-center">
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

    <!-- 商品瀑布流 -->
    <div class="container mx-auto p-2">
      <div class="column-container">
        <div v-for="product in productStore.products" :key="product.id" class="column-item">
            <ProductCard :product="product" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useProductStore } from '../stores/product.store';
import ProductCard from '../components/ProductCard.vue';
import { Search } from '@element-plus/icons-vue';

const productStore = useProductStore();
const router = useRouter();
const searchQuery = ref('');

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/products', query: { q: searchQuery.value } });
  }
};

</script>

<style scoped>
.column-container {
  column-count: 2;
  column-gap: 8px;
}

.column-item {
  break-inside: avoid;
  margin-bottom: 8px;
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

@media (min-width: 768px) {
  .column-container {
    column-count: 3;
  }
}
@media (min-width: 1024px) {
  .column-container {
    column-count: 4;
  }
}
@media (min-width: 1280px) {
  .column-container {
    column-count: 5;
  }
}
</style>
