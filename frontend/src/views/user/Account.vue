<template>
  <div class="account-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">我的账户</h1>
      </div>
    </header>

    <div class="p-4 space-y-4">
      <!-- Balance Card -->
      <el-card class="text-center">
        <p class="text-sm text-gray-500">账户余额 (元)</p>
        <p class="text-4xl font-bold my-4">{{ (userStore.profile?.balance ?? 0).toFixed(2) }}</p>
        <div class="flex space-x-4">
          <el-button class="w-1/2" size="large" @click="rechargeDialogVisible = true">充值</el-button>
          <el-button type="primary" class="w-1/2" size="large" @click="() => ElMessage.info('提现功能开发中')">提现</el-button>
        </div>
      </el-card>

      <!-- Transactions Card -->
      <el-card>
        <template #header>
          <div class="font-bold">近期交易</div>
        </template>
        <div v-if="userStore.transactions.length > 0" class="space-y-3">
          <div v-for="item in userStore.transactions" :key="item.id" class="flex justify-between items-center">
            <div>
              <p class="font-medium">{{ item.description }}</p>
              <p class="text-xs text-gray-400 mt-1">{{ new Date(item.createTime).toLocaleString('zh-CN') }}</p>
            </div>
            <p class="font-bold" :class="item.amount > 0 ? 'text-green-500' : 'text-red-500'">
              {{ item.amount > 0 ? '+' : '' }}{{ Number(item.amount).toFixed(2) }}
            </p>
          </div>
        </div>
        <el-empty v-else description="暂无交易记录"></el-empty>
      </el-card>
    </div>

    <!-- Recharge Dialog -->
    <el-dialog v-model="rechargeDialogVisible" title="充值" width="90%">
        <el-form label-position="top">
            <el-form-item label="充值金额">
                <el-input-number v-model="rechargeAmount" :precision="2" :step="10" :min="0" class="w-full"></el-input-number>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="rechargeDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleRecharge">确认充值</el-button>
            </span>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const rechargeDialogVisible = ref(false);
const rechargeAmount = ref(100);

const fetchAccountData = async () => {
  try {
    await userStore.fetchProfile();
    await userStore.fetchTransactions();
  } catch (error) {
    ElMessage.error('同步账户数据失败');
  }
};

onMounted(() => {
  fetchAccountData();
});

const handleRecharge = async () => {
    if (rechargeAmount.value <= 0) {
        ElMessage.warning('充值金额必须大于0');
        return;
    }
    try {
      await userStore.recharge(rechargeAmount.value);
    ElMessage.success('充值成功！');
    rechargeDialogVisible.value = false;
      fetchAccountData(); // 充值后立即刷新
    } catch (error) {
      ElMessage.error('充值失败，请稍后再试');
    }
}

</script>
