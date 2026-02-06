<template>
  <div class="my-orders-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">我的订单</h1>
      </div>
    </header>

    <div class="p-4">
        <el-tabs v-model="activeTab" class="custom-tabs" stretch @tab-change="activeStatusFilter = 'all'">
          <el-tab-pane label="我卖出的" name="sold">
            <!-- Order Status Filter -->
            <div class="grid grid-cols-6 text-center py-2 bg-white rounded-t-lg">
                <button v-for="status in statusFilters" :key="status.key" 
                        @click="activeStatusFilter = status.key"
                        class="text-gray-600 text-xs py-2" 
                        :class="{ 'font-semibold text-orange-500': activeStatusFilter === status.key }">
                    <p>{{ status.label }}</p>
                </button>
            </div>
            <div v-if="filteredSoldOrders.length > 0" class="space-y-4 mt-4">
              <div v-for="order in filteredSoldOrders" :key="order.id" 
                   class="bg-white rounded-lg shadow-sm overflow-hidden cursor-pointer"
                   @click="() => viewOrderDetail(order)">
                <div class="p-3 flex justify-between items-center border-b border-gray-100">
                    <div class="flex items-center">
                        <el-avatar :size="28" :src="order.buyer.avatarUrl" />
                        <span class="ml-2 text-sm font-medium">{{ order.buyer.name }}</span>
                    </div>
                    <span class="text-sm" :class="(order.status === '已完成' || order.status === '待评价') ? 'text-orange-500' : 'text-gray-500'">{{ (order.status === '已完成' || order.status === '待评价') ? '交易成功' : order.status }}</span>
                </div>
                <div class="p-3 bg-gray-50 flex space-x-3">
                    <img :src="order.product.imageUrl" :alt="order.product.title" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                    <p class="text-sm text-gray-700 line-clamp-2">{{ order.product.title }}</p>
                </div>
                <div class="p-3 text-right border-t border-gray-100">
                    <p class="text-sm">共1件 合计: <span class="text-lg font-bold text-red-500">¥{{ order.totalPrice }}</span></p>
                </div>
              </div>
            </div>
            <el-empty v-else :description="`暂无${activeStatusFilter === 'all' ? '' : statusFilters.find(f => f.key === activeStatusFilter)?.label}订单`"></el-empty>
          </el-tab-pane>

          <el-tab-pane label="我买到的" name="bought">
            <!-- Order Status Filter -->
            <div class="grid grid-cols-6 text-center py-2 bg-white rounded-t-lg">
                <button v-for="status in statusFilters" :key="status.key" 
                        @click="activeStatusFilter = status.key"
                        class="text-gray-600 text-xs py-2" 
                        :class="{ 'font-semibold text-orange-500': activeStatusFilter === status.key }">
                    <p>{{ status.label }}</p>
                </button>
            </div>
            <div v-if="filteredBoughtOrders.length > 0" class="space-y-4 mt-4">
              <div v-for="order in filteredBoughtOrders" :key="order.id" 
                   class="bg-white rounded-lg shadow-sm overflow-hidden cursor-pointer"
                   @click="() => viewOrderDetail(order)">
                <div class="p-3 flex justify-between items-center border-b border-gray-100">
                    <div class="flex items-center">
                        <el-avatar :size="28" :src="order.seller.avatarUrl" />
                        <span class="ml-2 text-sm font-medium">{{ order.seller.name }}</span>
                    </div>
                    <span class="text-sm" :class="(order.status === '已完成' || order.status === '待评价') ? 'text-orange-500' : 'text-gray-500'">{{ (order.status === '已完成' || order.status === '待评价') ? '交易成功' : order.status }}</span>
                </div>
                <div class="p-3 bg-gray-50 flex space-x-3">
                    <img :src="order.product.imageUrl" :alt="order.product.title" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                    <p class="text-sm text-gray-700 line-clamp-2">{{ order.product.title }}</p>
                </div>
                <div class="p-3 text-right border-t border-gray-100">
                    <p class="text-sm">共1件 合计: <span class="text-lg font-bold text-red-500">¥{{ order.totalPrice }}</span></p>
                </div>
              </div>
            </div>
            <el-empty v-else :description="`暂无${activeStatusFilter === 'all' ? '' : statusFilters.find(f => f.key === activeStatusFilter)?.label}订单`"></el-empty>
          </el-tab-pane>
        </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('sold');
const activeStatusFilter = ref('all');

const statusFilters = ref([
    { key: 'all', label: '全部' },
    { key: 'pending_payment', label: '待付款' },
    { key: 'pending_shipping', label: '待发货' },
    { key: 'shipped', label: '待收货' },
    { key: 'completed', label: '待评价' },
    { key: 'refund', label: '退款中' },
]);

const filteredSoldOrders = computed(() => {
    if (activeStatusFilter.value === 'all') return userStore.soldOrders;
    const filterLabel = statusFilters.value.find(f => f.key === activeStatusFilter.value)?.label;
    return userStore.soldOrders.filter(o => o.status === filterLabel);
});

const filteredBoughtOrders = computed(() => {
    if (activeStatusFilter.value === 'all') return userStore.boughtOrders;
    const filterLabel = statusFilters.value.find(f => f.key === activeStatusFilter.value)?.label;
    return userStore.boughtOrders.filter(o => o.status === filterLabel);
});

onMounted(() => {
    if (route.query.tab) activeTab.value = route.query.tab;
    if (route.query.filter) activeStatusFilter.value = route.query.filter;
});

watch([activeTab, activeStatusFilter], ([newTab, newFilter]) => {
    router.replace({ query: { tab: newTab, filter: newFilter } });
});

const viewOrderDetail = (order) => {
    router.push(`/user/order/${order.id}`);
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
