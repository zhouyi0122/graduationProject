<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-800 mb-6">数据仪表盘</h1>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-gray-500">总用户数</p>
              <p class="text-2xl font-bold">{{ stats.totalUsers }}</p>
            </div>
            <el-icon :size="40" class="text-blue-500"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-gray-500">总商品数</p>
              <p class="text-2xl font-bold">{{ stats.totalProducts }}</p>
            </div>
            <el-icon :size="40" class="text-green-500"><Goods /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-gray-500">总订单数</p>
              <p class="text-2xl font-bold">{{ stats.totalOrders }}</p>
            </div>
            <el-icon :size="40" class="text-orange-500"><Tickets /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-gray-500">今日订单新增</p>
              <p class="text-2xl font-bold">{{ stats.todayOrders }}</p>
            </div>
            <el-icon :size="40" class="text-red-500"><TrendCharts /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue';
import apiClient from '../../services/api';
import { ElMessage } from 'element-plus';
// For Element Plus icons
import { User, Goods, Tickets, TrendCharts } from '@element-plus/icons-vue';

const stats = reactive({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  todayOrders: 0
});

const fetchStats = async () => {
  try {
    const response = await apiClient.get('/admin/stats');
    Object.assign(stats, response.data);
  } catch (error) {
    console.error('获取统计数据失败:', error);
    ElMessage.error('无法加载仪表盘数据');
  }
};

onMounted(() => {
  fetchStats();
});
</script>












