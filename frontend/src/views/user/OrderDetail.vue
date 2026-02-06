<template>
  <div class="order-detail-page bg-gray-100 min-h-screen pb-20">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">订单详情</h1>
      </div>
    </header>

    <div v-if="order">
      <!-- Refund Detail View -->
      <div v-if="order.status === '退款中' || order.status === '退款成功' || order.status === '退款失败'" class="p-4 space-y-3">
        <!-- Refund Status Steps -->
        <el-card>
          <div class="flex justify-around items-center text-center">
            <div class="flex flex-col items-center text-orange-500">
              <div class="w-6 h-6 rounded-full bg-orange-500 text-white flex items-center justify-center mb-1">1</div>
              <span class="text-sm font-semibold">卖家处理</span>
            </div>
            <div class="flex-grow h-px bg-gray-200 mx-2"></div>
                        <div class="flex flex-col items-center" :class="order.refundInfo && order.refundInfo.disputeInProgress ? 'text-orange-500' : 'text-gray-400'">
              <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center mb-1" :class="order.refundInfo && order.refundInfo.disputeInProgress ? 'bg-orange-500 text-white border-orange-500' : 'border-gray-400'">2</div>
              <span class="text-sm" :class="{'font-semibold': order.refundInfo && order.refundInfo.disputeInProgress}">发起维权</span>
            </div>
            <div class="flex-grow h-px bg-gray-200 mx-2"></div>
            <div class="flex flex-col items-center" :class="['退款成功','退款失败'].includes(order.status) ? 'text-orange-500' : 'text-gray-400'">
              <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center mb-1" :class="['退款成功','退款失败'].includes(order.status) ? 'bg-orange-500 text-white border-orange-500' : 'border-gray-400'">3</div>
              <span class="text-sm" :class="{'font-semibold': ['退款成功','退款失败'].includes(order.status)}">退款结束</span>
            </div>
          </div>
        </el-card>

        <!-- Refund Progress Timeline -->
        <el-card>
          <h2 class="font-semibold mb-4">进度详情</h2>
          <el-timeline>
            <el-timeline-item v-for="(item, index) in order.refundInfo.history" :key="index" :timestamp="item.time">
              <template v-if="item.isRejection">
                <p>
                  <span v-if="isSellerView">您拒绝了买家的退款申请。</span>
                  <span v-else>卖家拒绝了您的退款申请。</span>
                  等待买家发起维权，
                  <Countdown v-if="item.rejectionTimestamp" :targetTimestamp="item.rejectionTimestamp" />
                  后超时未处理，退款将自动撤销。
                </p>
                <div class="text-xs text-gray-500 mt-1 pl-1">
                    <p>拒绝理由: {{ item.reason }}</p>
                </div>
              </template>
              <template v-else>
                <p>{{ isSellerView ? item.text.replace('您', '买家') : item.text }}</p>
                <div v-if="item.reason" class="text-xs text-gray-500 mt-1 pl-1">
                    <p>退款原因: {{ item.reason }}</p>
                    <p>退款说明: {{ item.description || '无' }}</p>
                </div>
              </template>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- Refund Info Card -->
        <el-card class="!p-0">
          <div class="p-3">
            <div class="flex space-x-3">
                <img :src="order.product.imageUrl" :alt="order.product.title" class="w-20 h-20 rounded-md object-cover flex-shrink-0" />
                <div class="flex-grow min-w-0">
                    <p class="text-sm text-gray-800 line-clamp-2">{{ order.product.title }}</p>
                </div>
            </div>
          </div>
          <div class="border-t border-gray-100 mx-3"></div>
          <div class="p-3 space-y-2 text-sm text-gray-600">
            <div class="flex justify-between"><span>退款金额:</span> <span class="font-bold text-red-500">¥{{ order.totalPrice }}</span></div>
            <div class="flex justify-between"><span>订单号:</span> <span>{{ order.orderNumber }}</span></div>
          </div>
        </el-card>
      </div>

      <!-- Regular Order Detail View -->
      <div v-else class="p-4 space-y-3">
        <el-card>
            <div @click="sections.timeline.expanded = !sections.timeline.expanded" class="flex justify-between items-center cursor-pointer">
            <p class="font-bold text-lg text-orange-500">{{ displayStatus }}</p>
            <el-icon v-if="order.status !== '退款中' && order.status !== '已取消'" class="transition-transform" :class="{ 'rotate-90': sections.timeline.expanded }"><ArrowRightBold /></el-icon>
            </div>
            <el-collapse-transition>
            <div v-show="sections.timeline.expanded && order.status !== '退款中' && order.status !== '已取消'" class="mt-4">
                <el-timeline>
                <el-timeline-item v-for="(activity, index) in timelineActivities" :key="index" :timestamp="activity.timestamp" :type="activity.type" :hollow="activity.hollow" :icon="activity.icon">
                    <span :class="{'text-gray-400': activity.hollow}">{{ activity.content }}</span>
                </el-timeline-item>
                </el-timeline>
            </div>
            </el-collapse-transition>
        </el-card>
        <el-card class="!p-0">
            <div class="p-3">
                <div class="flex space-x-3">
                    <img :src="order.product.imageUrl" :alt="order.product.title" class="w-20 h-20 rounded-md object-cover flex-shrink-0" />
                    <div class="flex-grow min-w-0">
                        <p class="text-sm text-gray-800 line-clamp-2">{{ order.product.title }}</p>
                        <p class="text-sm text-gray-500 mt-1">¥{{ order.product.price }}</p>
                    </div>
                </div>
            </div>
            <div class="border-t border-gray-100 mx-3"></div>
            <div class="p-3">
                <div @click="sections.price.expanded = !sections.price.expanded" class="flex justify-between items-center cursor-pointer">
                <p class="font-semibold">成交价</p>
                <div class="flex items-center">
                    <span class="text-lg font-bold text-red-500 mr-2">¥{{ order.totalPrice }}</span>
                    <el-icon class="transition-transform" :class="{ 'rotate-90': sections.price.expanded }"><ArrowRightBold /></el-icon>
                </div>
                </div>
                <el-collapse-transition>
                    <div v-show="sections.price.expanded" class="mt-4 pt-4 border-t border-dashed space-y-2 text-sm text-gray-600">
                        <div class="flex justify-between"><span>商品总价</span> <span>¥{{ order.priceDetails.productTotal }}</span></div>
                        <div class="flex justify-between"><span>运费</span> <span>¥{{ order.priceDetails.shippingFee }}</span></div>
                    </div>
                </el-collapse-transition>
            </div>
            <div class="border-t border-gray-100 mx-3"></div>
            <div class="p-3">
                <div @click="sections.info.expanded = !sections.info.expanded" class="flex justify-between items-center cursor-pointer">
                <p class="font-semibold">订单信息</p>
                <div class="flex items-center">
                    <span class="text-sm text-gray-400 mr-2">订单号: {{ order.orderNumber }}</span>
                    <el-icon class="transition-transform" :class="{ 'rotate-90': sections.info.expanded }"><ArrowRightBold /></el-icon>
                </div>
                </div>
                <el-collapse-transition>
                    <div v-show="sections.info.expanded" class="mt-4 pt-4 border-t border-dashed space-y-2 text-sm text-gray-600">
                        <div class="flex justify-between"><span>收货地址</span> <span>{{ order.shippingAddress }}</span></div>
                        <div class="flex justify-between"><span>{{ isSellerView ? '买家昵称' : '卖家昵称' }}</span> <span>{{ isSellerView ? order.buyer.name : order.seller.name }}</span></div>
                        <div class="flex justify-between"><span>下单时间</span> <span>{{ order.createTime }}</span></div>
                        <div v-if="order.payTime" class="flex justify-between"><span>付款时间</span> <span>{{ order.payTime }}</span></div>
                        <div v-if="order.shipTime" class="flex justify-between"><span>发货时间</span> <span>{{ order.shipTime }}</span></div>
                        <div v-if="order.completeTime" class="flex justify-between"><span>成交时间</span> <span>{{ order.completeTime }}</span></div>
                    </div>
                </el-collapse-transition>
            </div>
        </el-card>
      </div>
    </div>
    <el-empty v-else description="订单不存在"></el-empty>

    <!-- Bottom Action Bar -->
    <div v-if="order && !isAdminRoute" class="fixed bottom-0 bg-white border-t border-gray-200 h-16 flex items-center justify-between px-4 z-50 inset-x-0" :style="adminBarStyle">
        <button class="flex flex-col items-center text-gray-600" @click="startChat">
            <el-icon :size="24"><ChatDotRound /></el-icon>
            <span class="text-xs mt-1">{{ isSellerView ? '联系买家' : '联系卖家' }}</span>
        </button>
        <div v-if="order.status === '退款中'" class="flex items-center space-x-2">
            <template v-if="isSellerView">
                <el-button v-if="order.refundInfo && order.refundInfo.rejectionCount === 0" plain @click="rejectRefundDialogVisible = true">拒绝退款</el-button>
                <el-button type="primary" @click="handleApproveRefund">确认退款</el-button>
            </template>
            <template v-else>
                <el-button plain @click="handleDispute">我要维权</el-button>
                <el-button type="primary" @click="cancelRefund">撤销退款</el-button>
            </template>
        </div>
        <div v-else class="flex items-center space-x-2">
            <el-button v-if="!isSellerView && order.status === '待付款'" type="primary" @click="handlePay">去付款</el-button>
            <el-button v-if="!isSellerView && order.status === '待收货'" type="primary" @click="handleConfirmReceipt">确认收货</el-button>
            <el-button v-if="isSellerView && order.status === '待发货'" type="primary" @click="handleConfirmShipment">确认发货</el-button>
            <el-popover v-if="!isSellerView" placement="top-end" :width="100" trigger="click">
                <template #reference>
                    <el-button plain>更多</el-button>
                </template>
                <div class="flex flex-col space-y-2 text-center">
                    <button v-if="order.status === '待付款'" @click="handleCancelOrder" class="hover:text-orange-500">取消订单</button>
                    <button v-if="order.status !== '待付款' && order.status !== '已取消'" @click="refundDialogVisible = true" class="hover:text-orange-500">我要退款</button>
                    <button v-if="order.status === '已完成' || order.status === '待评价'" @click="handleDeleteOrder" class="hover:text-orange-500">删除订单</button>
                </div>
            </el-popover>
            <el-button v-if="!isSellerView && (order.status === '待评价' || order.status === '已完成')" type="primary" @click="openReviewDialog">{{ order.buyerReview ? '查看评价' : '去评价' }}</el-button>
            <el-button v-if="isSellerView && (order.status === '待评价' || order.status === '已完成')" type="primary" @click="openReviewDialog">{{ order.sellerReview ? '查看评价' : '去评价' }}</el-button>
        </div>
    </div>

            <div v-if="order && isAdminRoute" class="fixed bottom-0 bg-white border-t border-gray-200 h-16 flex items-center justify-between px-4 z-50 inset-x-0" :style="adminBarStyle">
        <div class="flex items-center space-x-2">
            <el-button plain @click="startChatWithUser(order.buyer)">联系买家</el-button>
            <el-button type="primary" @click="startChatWithUser(order.seller)">联系卖家</el-button>
        </div>
        <div v-if="order.status === '退款中'" class="flex items-center space-x-2">
            <el-button type="danger" @click="handleForceRefund">强制退款</el-button>
            <el-button type="warning" @click="handleForceClose">强制关单</el-button>
        </div>
    </div>

    <!-- Dialogs -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="90%">
        <el-form :model="refundForm" label-position="top">
            <el-form-item label="退款原因">
                <el-select v-model="refundForm.reason" placeholder="请选择退款原因" class="w-full">
                    <el-option label="不想要了" value="不想要了"></el-option>
                    <el-option label="商品信息描述不符" value="商品信息描述不符"></el-option>
                    <el-option label="商品损坏" value="商品损坏"></el-option>
                    <el-option label="其他" value="其他"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="退款说明">
                <el-input v-model="refundForm.description" type="textarea" :rows="4" placeholder="请详细描述您的问题"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="refundDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleApplyRefund">提交申请</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-model="rejectRefundDialogVisible" title="拒绝退款" width="90%">
        <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝退款的理由..."></el-input>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="rejectRefundDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleRejectRefund">确认拒绝</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" :title="isSellerView ? '评价买家' : '评价订单'" width="90%">
        <el-form :model="reviewForm" label-position="top">
            <el-form-item v-if="!isSellerView" label="评分">
                <el-rate v-model="reviewForm.rating" :colors="['#F56C6C', '#E6A23C', '#FF9900']" />
            </el-form-item>
            <el-form-item label="评价内容">
                <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="宝贝满足你的期待吗？说说你的使用心得吧～"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="reviewDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmitReview">提交评价</el-button>
            </span>
        </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, reactive, h, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { ArrowLeftBold, ArrowRightBold, ChatDotRound, Check } from '@element-plus/icons-vue';
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus';
import Countdown from '../../components/Countdown.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const order = ref(null);

const sections = reactive({
    timeline: { expanded: true },
    price: { expanded: false },
    info: { expanded: false },
});

const refundDialogVisible = ref(false);
const refundForm = reactive({
    reason: '',
    description: '',
});

const rejectRefundDialogVisible = ref(false);
const rejectReason = ref('');

const reviewDialogVisible = ref(false);
const reviewForm = reactive({
    rating: 5,
    comment: ''
});

const isSellerView = computed(() => order.value && order.value.id.startsWith('S'));

const displayStatus = computed(() => {
    if (!order.value) return '';
    if (order.value.status === '待评价' || order.value.status === '已完成') {
        return '交易成功';
    }
    return order.value.status;
});

const timelineActivities = computed(() => {
    if (!order.value) return [];
    const steps = ['拍下', '付款', '发货', '收货', '评价'];
    const orderStatusMap = {
        '待付款': '付款',
        '待发货': '发货',
        '待收货': '收货',
        '待评价': '评价',
        '已完成': '评价',
    };
    const currentStatusStep = orderStatusMap[order.value.status] || '拍下';
    const currentStepIndex = steps.indexOf(currentStatusStep);

    return steps.map((step, index) => {
        let content = '';
        if (index < currentStepIndex) {
            content = `已${step}`;
        } else {
            content = `待${step}`;
        }
        if (index === currentStepIndex) {
            content = order.value.status;
        }
        if ((isSellerView.value ? order.value.sellerReview : order.value.buyerReview) && step === '评价') {
            content = '已评价';
        }

        return {
            content: content,
            timestamp: '', // Simplified
            hollow: index > currentStepIndex,
            type: index <= currentStepIndex ? 'primary' : 'info',
            icon: index === currentStepIndex ? Check : null,
        };
    });
});

watch(() => route.params.id, (newId) => {
    if (newId) {
        const allOrders = [...userStore.soldOrders, ...userStore.boughtOrders];
        order.value = allOrders.find(o => o.id === newId) || null;
    }
}, { immediate: true });


const handleApplyRefund = () => {
    if (!refundForm.reason) {
        ElMessage.warning('请选择退款原因');
        return;
    }
    userStore.updateOrderStatus(order.value.id, '退款中', { ...refundForm });
    ElMessage.success('退款申请已提交');
    refundDialogVisible.value = false;
};

const cancelRefund = () => {
    ElMessageBox.confirm('您确定要撤销退款申请吗?', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.updateOrderStatus(order.value.id, order.value.previousStatus);
        ElMessage.success('退款已撤销');
    }).catch(() => {});
}

const handleApproveRefund = () => {
    ElMessageBox.confirm('您确定要同意该退款申请吗?', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.updateOrderStatus(order.value.id, '退款成功');
        ElMessage.success('已同意退款');
    }).catch(() => {});
}

const handleRejectRefund = () => {
    if (!rejectReason.value.trim()) {
        ElMessage.warning('请输入拒绝理由');
        return;
    }
    userStore.rejectRefund(order.value.id, rejectReason.value);
    ElMessage.success('已拒绝退款');
    rejectRefundDialogVisible.value = false;
    rejectReason.value = '';
}

const handlePay = () => {
    const price = parseFloat(order.value.totalPrice);
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
            <p class="text-gray-500">当前余额: ¥${userStore.profile.balance.toFixed(2)}</p>
            <p class="text-3xl font-bold my-2">¥${price.toFixed(2)}</p>
          </div>
        `,
        dangerouslyUseHTMLString: true,
      }
    ).then(() => {
        if (userStore.deductBalance(price)) {
            userStore.updateOrderStatus(order.value.id, '待发货');
            ElMessage.success('支付成功！');
        } else {
            ElMessage.error('余额不足，支付失败！');
        }
    }).catch(() => {});
}

const handleConfirmShipment = () => {
    ElMessageBox.confirm('您确定已发货吗？', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.updateOrderStatus(order.value.id, '待收货');
        ElMessage.success('操作成功');
    }).catch(() => {});
}

const handleConfirmReceipt = () => {
    ElMessageBox.confirm('请确认您已收到商品。', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.updateOrderStatus(order.value.id, '待评价');
        ElMessage.success('确认收货成功');
    }).catch(() => {});
}

const handleCancelOrder = () => {
    ElMessageBox.confirm('您确定要取消该订单吗？', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.updateOrderStatus(order.value.id, '已取消');
        ElMessage.info('订单已取消');
    }).catch(() => {});
}

const handleDeleteOrder = () => {
    ElMessageBox.confirm('删除后无法恢复，您确定要删除该订单吗？', '确认操作', { type: 'warning' })
    .then(() => {
        userStore.deleteOrder(order.value.id);
        ElMessage.success('订单已删除');
        router.back();
    }).catch(() => {});
}

const openReviewDialog = () => {
    const reviewTarget = isSellerView.value ? order.value.sellerReview : order.value.buyerReview;
    if (reviewTarget) {
        router.push(`/user/order/${order.value.id}/review`);
    } else {
        reviewDialogVisible.value = true;
    }
}

const handleSubmitReview = () => {
    if (!reviewForm.comment.trim()) {
        ElMessage.warning('请输入评价内容');
        return;
    }
    if (isSellerView.value) {
        userStore.submitSellerReview(order.value.id, reviewForm.comment);
    } else {
        userStore.submitBuyerReview(order.value.id, { ...reviewForm });
    }
    ElMessage.success('评价成功');
    reviewDialogVisible.value = false;
}

const isAdminRoute = computed(() => router.currentRoute.value.path.startsWith('/admin'));
const adminBarStyle = computed(() => isAdminRoute.value ? 'left: 200px;' : 'left: 0;');

const startChatWithUser = (user) => {
    const conversationId = userStore.getOrCreateConversationBySeller(user);
    router.push({ path: `/chat/${conversationId}`, query: { from: router.currentRoute.value.fullPath } });
};

const startChat = () => {
    const targetUser = isSellerView.value ? order.value.buyer : order.value.seller;
    const conversationId = userStore.getOrCreateConversationBySeller(targetUser);
    router.push({ path: `/chat/${conversationId}`, query: { from: router.currentRoute.value.fullPath } });
}

const handleForceRefund = () => {
    ElMessageBox.confirm('您确定要强制完成此退款吗？此操作不可逆。', '确认强制退款', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(() => {
        userStore.forceRefundByAdmin(order.value.id);
        ElMessage.success('已强制退款');
    }).catch(() => {
        ElMessage.info('已取消操作');
    });
}

const handleForceClose = () => {
    ElMessageBox.confirm('您确定要强制关单吗？此操作不可逆，本次退款将标记为失败。', '确认强制关单', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(() => {
        userStore.forceCloseRefundByAdmin(order.value.id);
        ElMessage.success('已强制关单');
    }).catch(() => {
        ElMessage.info('已取消操作');
    });
}

const handleForceRefund_OLD = () => {
    ElMessageBox.confirm('您确定要强制完成此退款吗？此操作不可逆。', '确认强制退款', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(() => {
        userStore.forceRefundByAdmin(order.value.id);
        ElMessage.success('已强制退款');
    }).catch(() => {
        ElMessage.info('已取消操作');
    });
}

const handleDispute = () => {
    const conversationId = userStore.requestDispute(order.value.id);
    if (conversationId) {
        router.push({ path: `/chat/${conversationId}`, query: { from: router.currentRoute.value.fullPath } });
    } else {
        ElMessage.error('发起维权失败，请稍后再试。');
    }
}

</script>

<style scoped>
.line-clamp-2 {
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

:deep(.el-timeline-item__node--primary) {
    background-color: #f97316 !important;
}
</style>
