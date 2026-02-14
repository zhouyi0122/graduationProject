<template>
  <div class="order-confirmation-page bg-gray-100 min-h-screen pb-20">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="handleCancel" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">确认订单</h1>
      </div>
    </header>

    <div v-if="product" class="p-4 space-y-4">
        <!-- Shipping Address -->
        <el-card class="cursor-pointer hover:bg-gray-50" @click="addressDialogVisible = true">
            <div class="flex items-center">
                <el-icon :size="20" class="text-gray-500"><Location /></el-icon>
                <div class="ml-4 flex-grow">
                    <div class="flex justify-between">
                        <p class="font-semibold">{{ shippingInfo.name }} <span class="text-gray-500 font-normal ml-2">{{ shippingInfo.phone }}</span></p>
                    </div>
                    <p class="text-sm text-gray-600 mt-1">{{ shippingInfo.address }}</p>
                </div>
                <el-icon class="text-gray-400"><ArrowRightBold /></el-icon>
            </div>
        </el-card>

        <!-- Product Info -->
        <el-card>
            <div class="flex space-x-4">
                <img :src="product.images[0].src" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0" />
                <div class="flex-grow min-w-0">
                    <p class="text-sm text-gray-800 line-clamp-2 font-semibold">{{ product.title }}</p>
                    <p class="text-sm text-gray-500 mt-1">卖家: {{ product.seller.name }}</p>
                    <p class="text-lg font-bold text-red-500 mt-2">¥{{ product.price }}</p>
                </div>
            </div>
        </el-card>

        <!-- Price Details -->
        <el-card>
            <div class="space-y-3 text-sm">
                <div class="flex justify-between"><span>商品总价</span> <span>¥{{ product.price }}</span></div>
                <div class="flex justify-between"><span>运费</span> <span>¥0.00</span></div>
            </div>
        </el-card>
    </div>
    <el-empty v-else description="商品信息不存在"></el-empty>

    <!-- Bottom Action Bar -->
    <div v-if="product" class="fixed inset-x-0 bottom-0 bg-white border-t border-gray-200 h-16 flex items-center justify-end px-4 z-50">
        <div class="flex items-center space-x-3">
            <p class="text-sm">合计: <span class="text-xl font-bold text-red-500">¥{{ product.price }}</span></p>
            <el-button type="primary" size="large" @click="handleConfirmPayment">确认支付</el-button>
        </div>
    </div>

    <!-- Address Edit Dialog -->
    <el-dialog v-model="addressDialogVisible" title="编辑收货地址" width="90%">
        <el-form :model="editableAddress" label-position="top">
            <el-form-item label="收货人">
                <el-input v-model="editableAddress.name" />
            </el-form-item>
            <el-form-item label="手机号码">
                <el-input v-model="editableAddress.phone" />
            </el-form-item>
            <el-form-item label="详细地址">
                <el-input v-model="editableAddress.address" type="textarea" :rows="3" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="addressDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveAddress">保存</el-button>
            </span>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import apiClient from '../services/api';
import { ArrowLeftBold, ArrowRightBold, Location } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const product = ref(null);
const shippingInfo = reactive({ name: '', phone: '', address: '' });
const addressDialogVisible = ref(false);
const editableAddress = reactive({ name: '', phone: '', address: '' });
const loading = ref(false);

onMounted(() => {
    try {
        if (route.query.product) {
            product.value = JSON.parse(route.query.product);
            
            // 优先使用 store 中的地址，如果没有则留空
            const storeAddress = userStore.profile.shippingInfo || { name: '', phone: '', address: '' };
            Object.assign(shippingInfo, storeAddress);
            Object.assign(editableAddress, storeAddress);
        }
    } catch (e) {
        console.error('Failed to parse product data from query:', e);
    }
});

const saveAddress = () => {
    if (!editableAddress.name || !editableAddress.phone || !editableAddress.address) {
        ElMessage.warning('请填写完整的收货信息');
        return;
    }
    Object.assign(shippingInfo, editableAddress);
    addressDialogVisible.value = false;
    ElMessage.success('地址已更新');
}

const handleConfirmPayment = () => {
    if (!shippingInfo.name || !shippingInfo.phone || !shippingInfo.address) {
        ElMessage.warning('请先完善收货地址');
        addressDialogVisible.value = true;
        return;
    }

    const price = parseFloat(product.value.price);
    ElMessageBox.confirm(
      `将从您的余额中支付 ¥${price.toFixed(2)}。`, 
      '确认支付',
      { 
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'info',
        center: true,
        message: `
          <div class="text-center">
            <p class="text-gray-500">当前余额: ¥${(userStore.profile?.balance || 0).toFixed(2)}</p>
            <p class="text-3xl font-bold my-2">¥${price.toFixed(2)}</p>
          </div>
        `,
        dangerouslyUseHTMLString: true,
      }
    ).then(async () => {
        loading.value = true;
        try {
            const orderPayload = {
                productId: product.value.id,
                shippingName: shippingInfo.name,
                shippingPhone: shippingInfo.phone,
                shippingAddress: shippingInfo.address
            };

            const response = await apiClient.post('/orders', orderPayload);
            const createdOrder = response.data;

            ElMessage.success('支付成功！');
            // 下单成功后跳转到订单详情页，带上真实的订单ID
            router.replace(`/user/order/${createdOrder.id}`);
        } catch (error) {
            console.error('下单失败:', error);
            const errMsg = error.response?.data || '支付失败，请检查余额或登录状态';
            ElMessage.error(errMsg);
        } finally {
            loading.value = false;
        }
    }).catch(() => {});
};

const handleCancel = () => {
    ElMessageBox.confirm(
      '确认要取消支付吗？内容将不会被保存。',
      '提示',
      { type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '继续支付' }
    ).then(() => {
        router.back();
    }).catch(() => {});
};

</script>

<style scoped>
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}
:deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px #f97316 inset !important;
}
</style>
