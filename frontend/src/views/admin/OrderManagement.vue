<template>
  <div class="order-management-page">
    <h1 class="text-2xl font-bold mb-4">订单管理</h1>
    <el-card v-loading="loading">
      <div class="mb-4">
        <el-input v-model="searchQuery" placeholder="按订单号搜索" clearable size="large" />
      </div>
      <el-table :data="filteredOrders" style="width: 100%" @row-click="viewOrderDetail" class="cursor-pointer">
        <el-table-column prop="orderNumber" label="订单号" width="220"></el-table-column>
        <el-table-column prop="productTitle" label="商品标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="buyerNickname" label="买家昵称"></el-table-column>
        <el-table-column prop="sellerNickname" label="卖家昵称"></el-table-column>
        <el-table-column prop="totalPrice" label="成交价" width="120">
          <template #default="scope">
            <span class="font-bold text-red-500">¥{{ scope.row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="scope">
            <div class="flex flex-col space-y-1">
              <el-tag :type="getStatusTagType(scope.row)">
                {{ getStatusLabel(scope.row) }}
              </el-tag>
              <el-tag v-if="scope.row.disputeInProgress === 1" type="danger" effect="dark" size="small">
                维权中
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <div class="flex space-x-2">
              <el-button size="small" @click.stop="() => viewOrderDetail(scope.row)">详情</el-button>
              <template v-if="scope.row.disputeInProgress === 1">
                <el-button size="small" type="success" @click.stop="() => handleDispute(scope.row, 'force_refund')">强制退款</el-button>
                <el-button size="small" type="danger" @click.stop="() => handleDispute(scope.row, 'close_refund')">驳回</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';
import apiClient from '../../services/api';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const searchQuery = ref('');

const getStatusLabel = (order) => {
  const status = order.status;
  if (status === 8) {
    return order.disputeInProgress === 2 ? '退款失败' : '卖家拒绝退款';
  }
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '待评价',
    4: '已完成',
    5: '退款中',
    6: '已取消',
    7: '退款成功'
  };
  return map[status] || '未知状态';
};

const getStatusTagType = (order) => {
  const status = order.status;
  switch (status) {
    case 0: return 'warning';
    case 1:
    case 2: return 'primary';
    case 3:
    case 4: return 'success';
    case 5: return 'danger';
    case 6: return 'info';
    case 7: return 'success';
    case 8: return 'info';
    default: return 'info';
  }
};

const loadOrders = async () => {
  loading.value = true;
  try {
    await userStore.fetchAllAdminOrders();
  } catch (error) {
    console.error('加载订单列表失败:', error);
    ElMessage.error('无法拉取订单数据');
  } finally {
    loading.value = false;
  }
};

onMounted(loadOrders);

const filteredOrders = computed(() => {
  if (!searchQuery.value) {
    return userStore.allOrders;
  }
  return userStore.allOrders.filter(order => 
    order.orderNumber && order.orderNumber.includes(searchQuery.value)
  );
});

const viewOrderDetail = (order) => {
    router.push(`/admin/orders/${order.id}`);
}

const handleDispute = (order, action) => {
  const actionText = action === 'force_refund' ? '强制退款' : '驳回维权';
  const confirmMessage = action === 'force_refund' 
    ? `确定要对订单 ${order.orderNumber} 执行强制退款吗？资金将退回到买家余额。` 
    : `确定要驳回订单 ${order.orderNumber} 的维权申请吗？`;

  ElMessageBox.confirm(confirmMessage, '维权处理', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: action === 'force_refund' ? 'warning' : 'info'
  }).then(async () => {
    try {
      await apiClient.put(`/admin/orders/${order.id}/refund/handle`, { action });
      ElMessage.success(`${actionText}处理成功`);
      loadOrders(); // 刷新列表
    } catch (error) {
      console.error('维权处理失败:', error);
      ElMessage.error(error.response?.data?.message || '操作失败');
    }
  }).catch(() => {});
}
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
