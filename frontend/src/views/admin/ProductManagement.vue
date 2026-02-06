<template>
  <div class="product-management-page">
    <h1 class="text-2xl font-bold mb-4">商品管理</h1>
    <el-card>
      <el-table :data="userStore.allProducts" style="width: 100%" @row-click="viewProductDetail" class="cursor-pointer">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="seller" label="卖家"></el-table-column>
        <el-table-column prop="category" label="分类"></el-table-column>
        <el-table-column prop="price" label="价格"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在售' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" :type="scope.row.status === '在售' ? 'danger' : 'success'" @click.stop="() => handleToggleStatus(scope.row)">
              {{ scope.row.status === '在售' ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const viewProductDetail = (row, column) => {
    // Prevent navigation when clicking on the action column
    if (column && column.label === '操作') return;
    router.push(`/product/${row.id}`);
}

const handleToggleStatus = (product) => {
    const actionText = product.status === '在售' ? '下架' : '上架';
    ElMessageBox.confirm(`您确定要${actionText}该商品吗？`, '确认操作', { type: 'warning' })
    .then(() => {
        userStore.adminToggleProductStatus(product.id);
        ElMessage.success(`${actionText}成功`);
    }).catch(() => {});
}
</script>
