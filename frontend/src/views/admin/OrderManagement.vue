<template>
  <div class="order-management-page">
    <h1 class="text-2xl font-bold mb-4">订单管理</h1>
    <el-card>
      <div class="mb-4">
        <el-input v-model="searchQuery" placeholder="按订单号搜索" clearable size="large" />
      </div>
      <el-table :data="filteredOrders" style="width: 100%">
        <el-table-column prop="orderNumber" label="订单号" width="180"></el-table-column>
        <el-table-column prop="product.title" label="商品标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="buyer.name" label="买家昵称"></el-table-column>
        <el-table-column prop="seller.name" label="卖家昵称"></el-table-column>
        <el-table-column prop="totalPrice" label="价格">
          <template #default="scope">
            <span>¥{{ scope.row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="() => viewOrderDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';

const router = useRouter();
const userStore = useUserStore();

const searchQuery = ref('');

const allOrders = computed(() => [...userStore.soldOrders, ...userStore.boughtOrders]);

const filteredOrders = computed(() => {
  if (!searchQuery.value) {
    return allOrders.value;
  }
  return allOrders.value.filter(order => 
    order.orderNumber.includes(searchQuery.value)
  );
});

const getStatusTagType = (status) => {
  switch (status) {
    case '待付款': return 'warning';
    case '待发货': return 'primary';
    case '待收货': return 'primary';
    case '已完成':
    case '待评价': return 'success';
    case '退款中': return 'danger';
    case '已取消': return 'info';
    default: return 'info';
  }
}

const viewOrderDetail = (order) => {
    router.push(`/admin/orders/${order.id}`);
}

</script>
