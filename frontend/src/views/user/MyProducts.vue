<template>
  <div class="my-products-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">我的发布</h1>
        <button @click="() => router.push('/products/new')" class="absolute right-2 p-2 text-orange-500 font-semibold">发布</button>
      </div>
    </header>

    <div class="p-4">
        <el-tabs v-model="activeTab" class="custom-tabs" stretch>
          <el-tab-pane label="在卖" name="on_sale">
            <div v-if="onSaleProducts.length > 0" class="space-y-4">
              <div v-for="product in onSaleProducts" :key="product.id" 
                   class="bg-white rounded-lg shadow-sm p-3 flex space-x-3 cursor-pointer"
                   @click="viewProductDetail(product.id)">
                <img :src="product.imageUrl" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0" />
                <div class="flex flex-col justify-between flex-grow min-w-0">
                    <div>
                        <p class="text-sm text-gray-800 line-clamp-2">{{ product.title }}</p>
                        <p class="text-lg font-bold text-red-500 mt-1">¥{{ product.price }}</p>
                    </div>
                    <div class="text-right">
                        <el-button size="small" plain @click.stop="editProduct(product.id)">编辑</el-button>
                        <el-button size="small" type="danger" plain @click.stop="handleDelist(product.id)">下架</el-button>
                    </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无在卖商品"></el-empty>
          </el-tab-pane>

          <el-tab-pane label="草稿箱" name="drafts">
             <div v-if="draftProducts.length > 0" class="space-y-4">
              <div v-for="product in draftProducts" :key="product.id" class="bg-white rounded-lg shadow-sm p-3 flex space-x-3">
                <img :src="product.imageUrl" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0" />
                <div class="flex flex-col justify-between flex-grow min-w-0">
                    <div>
                        <p class="text-sm text-gray-800 line-clamp-2">{{ product.title }}</p>
                        <p class="text-lg font-bold text-red-500 mt-1">¥{{ product.price }}</p>
                    </div>
                    <div class="text-right">
                        <el-button size="small" type="primary" @click.stop="editProduct(product.id)">继续编辑</el-button>
                        <el-button size="small" type="danger" plain @click.stop="handleDelete(product.id, true)">删除</el-button>
                    </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="草稿箱是空的"></el-empty>
          </el-tab-pane>

          <el-tab-pane label="已下架" name="delisted">
            <div v-if="delistedProducts.length > 0" class="space-y-4">
              <div v-for="product in delistedProducts" :key="product.id" class="bg-white rounded-lg shadow-sm p-3 flex space-x-3 opacity-70">
                <img :src="product.imageUrl" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0" />
                <div class="flex flex-col justify-between flex-grow min-w-0">
                    <div>
                        <p class="text-sm text-gray-800 line-clamp-2">{{ product.title }}</p>
                        <p class="text-lg font-bold text-red-500 mt-1">¥{{ product.price }}</p>
                    </div>
                    <div class="text-right">
                        <el-button size="small" type="danger" plain @click.stop="handleDelete(product.id)">删除记录</el-button>
                    </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无已下架商品"></el-empty>
          </el-tab-pane>
        </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const activeTab = ref('on_sale');

const onSaleProducts = computed(() => userStore.myProducts.filter(p => p.status === 'published'));
const draftProducts = computed(() => userStore.drafts);
const delistedProducts = computed(() => userStore.myProducts.filter(p => p.status === 'delisted'));

onMounted(() => {
  if (route.query.tab === 'drafts') {
    activeTab.value = 'drafts';
  }
});

const viewProductDetail = (productId) => {
    router.push(`/product/${productId}`);
}

const editProduct = (productId) => {
    router.push(`/product/edit/${productId}`);
}

const handleDelist = (productId) => {
    ElMessageBox.confirm('确定要下架该商品吗？', '提示', { type: 'warning' })
    .then(() => {
        userStore.delistProduct(productId);
        ElMessage.success('已下架');
    }).catch(() => {});
}

const handleDelete = (productId, isDraft = false) => {
    const message = isDraft ? '确定要删除该草稿吗？' : '删除后不可恢复，确定要删除该记录吗？';
    ElMessageBox.confirm(message, '提示', { type: 'warning' })
    .then(() => {
        userStore.deleteProduct(productId, isDraft);
        ElMessage.success('已删除');
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

.custom-tabs {
    background-color: white;
    border-radius: 8px;
    padding: 0 16px;
}
.custom-tabs > :deep(.el-tabs__content) {
  padding: 16px 0;
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
