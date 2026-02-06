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
        <p class="text-4xl font-bold my-4">{{ userStore.profile.balance.toFixed(2) }}</p>
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
        <div v-if="transactions.length > 0" class="space-y-3">
          <div v-for="item in transactions" :key="item.id" class="flex justify-between items-center">
            <div>
              <p class="font-medium">{{ item.description }}</p>
              <p class="text-xs text-gray-400 mt-1">{{ item.timestamp }}</p>
            </div>
            <p class="font-bold" :class="item.amount > 0 ? 'text-green-500' : 'text-red-500'">
              {{ item.amount > 0 ? '+' : '' }}{{ item.amount.toFixed(2) }}
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const rechargeDialogVisible = ref(false);
const rechargeAmount = ref(100);

const transactions = ref([
  { id: 1, description: '卖出商品 - 考研数学资料', amount: 60.00, timestamp: '2023-10-25 11:30' },
  { id: 2, description: '购买商品 - 九成新山地车', amount: -288.00, timestamp: '2023-10-24 09:15' },
  { id: 3, description: '提现', amount: -500.00, timestamp: '2023-10-23 18:00' },
  { id: 4, description: '充值', amount: 1000.00, timestamp: '2023-10-22 12:00' },
]);

const handleRecharge = () => {
    if (rechargeAmount.value <= 0) {
        ElMessage.warning('充值金额必须大于0');
        return;
    }
    userStore.recharge(rechargeAmount.value);
    // Add to transaction history
    transactions.value.unshift({
        id: Date.now(),
        description: '充值',
        amount: rechargeAmount.value,
        timestamp: new Date().toLocaleString('zh-CN'),
    });
    ElMessage.success('充值成功！');
    rechargeDialogVisible.value = false;
}

</script>
