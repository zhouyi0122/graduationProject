<template>
  <div class="product-management-page">
    <h1 class="text-2xl font-bold mb-4">商品管理</h1>
    <el-card v-loading="loading">
      <el-table :data="productStore.products" style="width: 100%" @row-click="viewProductDetail" class="cursor-pointer">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
        <el-table-column label="卖家">
          <template #default="scope">
            {{ scope.row.seller?.nickname || '卖家ID: ' + scope.row.sellerId }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.status === 0 ? 'danger' : 'success'" 
              :disabled="scope.row.status === 1"
              @click.stop="() => handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 0 ? '强制下架' : '重新上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useProductStore } from '../../stores/product.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const productStore = useProductStore();
const loading = ref(false);

const getStatusLabel = (status) => {
  const map = { 0: '在售', 1: '已售出', 2: '已下架' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'info', 2: 'danger' };
  return map[status] || 'info';
};

const loadProducts = async () => {
  loading.value = true;
  try {
    await productStore.fetchAllAdminProducts();
  } catch (error) {
    ElMessage.error('加载商品列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(loadProducts);

const viewProductDetail = (row, column) => {
    if (column && column.label === '操作') return;
    router.push(`/product/${row.id}`);
}

const handleToggleStatus = (product) => {
    const isOffShelf = product.status === 0;
    const actionText = isOffShelf ? '下架' : '上架';
    
    ElMessageBox.confirm(`您确定要${actionText}商品 “${product.title}” 吗？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    .then(async () => {
        try {
          await productStore.adminToggleProductStatus(product.id);
          ElMessage.success(`商品已${actionText}`);
        } catch (error) {
          ElMessage.error(`${actionText}失败，请稍后再试`);
        }
    }).catch(() => {});
}
</script>
