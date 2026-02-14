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
                        <el-avatar :size="28" :src="`https://picsum.photos/100/100?random=u${order.buyerId}`" />
                        <span class="ml-2 text-sm font-medium">买家ID: {{ order.buyerId }}</span>
                    </div>
                    <span class="text-sm" :class="(order.status === 3 || order.status === 4) ? 'text-orange-500' : 'text-gray-500'">
                        {{ getStatusLabel(order) }}
                    </span>
                </div>
                <div class="p-3 bg-gray-50 flex space-x-3">
                    <img :src="order.imageUrl || 'https://picsum.photos/200/200?random=' + order.productId" :alt="order.productTitle" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                    <p class="text-sm text-gray-700 line-clamp-2">{{ order.productTitle }}</p>
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
                        <el-avatar :size="28" :src="`https://picsum.photos/100/100?random=u${order.sellerId}`" />
                        <span class="ml-2 text-sm font-medium">卖家ID: {{ order.sellerId }}</span>
                    </div>
                    <span class="text-sm" :class="(order.status === 3 || order.status === 4) ? 'text-orange-500' : 'text-gray-500'">
                        {{ getStatusLabel(order) }}
                    </span>
                </div>
                <div class="p-3 bg-gray-50 flex space-x-3">
                    <img :src="order.imageUrl || 'https://picsum.photos/200/200?random=' + order.productId" :alt="order.productTitle" class="w-16 h-16 rounded-md object-cover flex-shrink-0" />
                    <p class="text-sm text-gray-700 line-clamp-2">{{ order.productTitle }}</p>
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
    { key: '0', label: '待付款' },
    { key: '1', label: '待发货' },
    { key: '2', label: '待收货' },
    { key: '3', label: '待评价' },
    { key: '5', label: '退款中' },
]);

// 状态映射表
const getStatusLabel = (order) => {
    const status = order.status;
    // 如果正在维权中，优先显示维权中状态
    if (order.disputeInProgress === 1) {
        return '维权中';
    }
    const map = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '待评价',
        4: '已完成',
        5: '退款中',
        6: '已取消',
        7: '退款成功',
        8: order.disputeInProgress === 2 ? '退款失败' : '卖家拒绝退款'
    };
    return map[status] || '未知状态';
};

const filteredSoldOrders = computed(() => {
    if (activeStatusFilter.value === 'all') return userStore.soldOrders;
    if (activeStatusFilter.value === '5') {
        // 退款中标签：包含“退款中(5)”和“卖家拒绝退款(8且未被管理员驳回)”
        return userStore.soldOrders.filter(o => o.status === 5 || (o.status === 8 && o.disputeInProgress !== 2));
    }
    return userStore.soldOrders.filter(o => String(o.status) === activeStatusFilter.value);
});

const filteredBoughtOrders = computed(() => {
    if (activeStatusFilter.value === 'all') return userStore.boughtOrders;
    if (activeStatusFilter.value === '5') {
        // 退款中标签：包含“退款中(5)”和“卖家拒绝退款(8且未被管理员驳回)”
        return userStore.boughtOrders.filter(o => o.status === 5 || (o.status === 8 && o.disputeInProgress !== 2));
    }
    return userStore.boughtOrders.filter(o => String(o.status) === activeStatusFilter.value);
});

const loadOrders = async () => {
    try {
        await userStore.fetchOrders(activeTab.value);
    } catch (error) {
        ElMessage.error('加载订单列表失败');
    }
};

onMounted(() => {
    if (route.query.tab) activeTab.value = route.query.tab;
    if (route.query.filter) activeStatusFilter.value = route.query.filter;
    loadOrders();
});

watch(activeTab, () => {
    loadOrders();
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
