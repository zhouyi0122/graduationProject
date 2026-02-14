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

    <div v-if="order" class="p-4 space-y-3">
      <!-- Back Button Offset for Admin -->
      <button @click="router.back()" 
              class="fixed top-4 z-50 h-10 w-10 bg-black/30 text-white rounded-full flex items-center justify-center"
              :style="{ left: isAdminRoute ? '216px' : '16px' }">
        <el-icon><ArrowLeftBold /></el-icon>
      </button>

      <!-- 1. 退款状态及进度 (状态为 5:退款中, 7:退款成功, 8:退款失败) -->
      <template v-if="[5, 7, 8].includes(order.status)">
        <!-- 退款步骤条 -->
        <el-card>
          <div class="flex justify-around items-center text-center">
            <!-- 步骤 1: 买家申请 -->
            <div class="flex flex-col items-center text-orange-500">
              <div class="w-6 h-6 rounded-full bg-orange-500 text-white flex items-center justify-center mb-1 text-xs">1</div>
              <span class="text-xs font-semibold">买家申请</span>
            </div>
            <div class="flex-grow h-px bg-gray-200 mx-1"></div>
            
            <!-- 步骤 2: 卖家处理 -->
            <div class="flex flex-col items-center" :class="order.status !== 5 || order.disputeInProgress === 1 ? 'text-orange-500' : 'text-gray-400'">
              <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center mb-1 text-xs" :class="order.status !== 5 || order.disputeInProgress === 1 ? 'bg-orange-500 text-white border-orange-500' : 'border-gray-400'">2</div>
              <span class="text-xs">卖家处理</span>
            </div>
            <div class="flex-grow h-px bg-gray-200 mx-1"></div>

            <!-- 步骤 3: 管理员处理中 (维权) -->
            <div class="flex flex-col items-center" :class="order.disputeInProgress === 1 || (order.status === 8 && order.disputeInProgress === 2) || (order.status === 7 && order.refundInfo?.history?.some(h => h.text.includes('管理员'))) ? 'text-orange-500' : 'text-gray-400'">
              <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center mb-1 text-xs" :class="order.disputeInProgress === 1 || (order.status === 8 && order.disputeInProgress === 2) || (order.status === 7 && order.refundInfo?.history?.some(h => h.text.includes('管理员'))) ? 'bg-orange-500 text-white border-orange-500' : 'border-gray-400'">3</div>
              <span class="text-xs">管理员处理</span>
            </div>
            <div class="flex-grow h-px bg-gray-200 mx-1"></div>
            
            <!-- 步骤 4: 退款结束 -->
            <div class="flex flex-col items-center" :class="[7, 8].includes(order.status) && (order.status === 7 || order.disputeInProgress === 2) ? 'text-orange-500' : 'text-gray-400'">
              <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center mb-1 text-xs" :class="[7, 8].includes(order.status) && (order.status === 7 || order.disputeInProgress === 2) ? 'bg-orange-500 text-white border-orange-500' : 'border-gray-400'">4</div>
              <span class="text-xs">退款结束</span>
            </div>
          </div>
        </el-card>

        <!-- 退款进度时间轴 -->
        <el-card v-if="order.refundInfo?.history">
          <h2 class="font-semibold mb-4 text-sm">退款进度</h2>
          <el-timeline>
            <el-timeline-item v-for="(item, index) in order.refundInfo.history" :key="index" :timestamp="new Date(item.createTime).toLocaleString()" placement="top">
              <p class="text-sm font-medium text-gray-800">{{ getRefundHistoryText(item.text) }}</p>
              <div v-if="item.reason" class="text-xs text-gray-500 mt-1 bg-gray-50 p-2 rounded">
                  <p>原因: {{ item.reason }}</p>
                  <p v-if="item.description">说明: {{ item.description }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </template>

      <!-- 2. 普通进度时间轴 (非退款状态) -->
      <el-card v-else>
          <div @click="sections.timeline.expanded = !sections.timeline.expanded" class="flex justify-between items-center cursor-pointer">
              <p class="font-bold text-lg text-orange-500">{{ displayStatus }}</p>
              <el-icon v-if="order.status !== 5 && order.status !== 6" class="transition-transform" :class="{ 'rotate-90': sections.timeline.expanded }"><ArrowRightBold /></el-icon>
          </div>
          <el-collapse-transition>
              <div v-show="sections.timeline.expanded" class="mt-4">
                  <el-timeline>
                      <el-timeline-item v-for="(activity, index) in timelineActivities" :key="index" 
                          :type="activity.type" :hollow="activity.hollow" :icon="activity.icon">
                          <span :class="{'text-gray-400': activity.hollow}">{{ activity.content }}</span>
                      </el-timeline-item>
                  </el-timeline>
              </div>
          </el-collapse-transition>
      </el-card>

      <!-- 3. 商品及价格信息 -->
      <el-card class="!p-0" v-if="product">
          <div class="p-3">
              <div class="flex space-x-3" @click="router.push(`/product/${product.id}`)">
                  <img :src="product.imageUrl" :alt="product.title" class="w-24 h-24 rounded-md object-cover flex-shrink-0" />
                  <div class="flex flex-col justify-between flex-grow min-w-0">
                      <p class="text-sm text-gray-800 line-clamp-2 font-semibold">{{ product.title }}</p>
                      <p class="text-sm text-gray-500 mt-1">¥{{ product.price }}</p>
                  </div>
              </div>
          </div>
          <div class="border-t border-gray-100 mx-3"></div>
          <div class="p-3">
              <div @click="sections.price.expanded = !sections.price.expanded" class="flex justify-between items-center cursor-pointer">
                  <p class="font-semibold">实付款</p>
                  <div class="flex items-center">
                      <span class="text-lg font-bold text-red-500 mr-2">¥{{ order.totalPrice }}</span>
                      <el-icon class="transition-transform" :class="{ 'rotate-90': sections.price.expanded }"><ArrowRightBold /></el-icon>
                  </div>
              </div>
              <el-collapse-transition>
                  <div v-show="sections.price.expanded" class="mt-4 pt-4 border-t border-dashed space-y-2 text-sm text-gray-600">
                      <div class="flex justify-between"><span>商品总价</span> <span>¥{{ product.price }}</span></div>
                      <div v-if="[5, 7, 8].includes(order.status)" class="flex justify-between text-red-500">
                        <span>申请退款金额</span> <span>¥{{ order.totalPrice }}</span>
                      </div>
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
                      <div class="flex justify-between"><span>收货人</span> <span>{{ order.shippingName }}</span></div>
                      <div class="flex justify-between"><span>联系电话</span> <span>{{ order.shippingPhone }}</span></div>
                      <div class="flex justify-between"><span>{{ isSellerView ? '买家' : '卖家' }}昵称</span> <span>{{ isSellerView ? buyer?.nickname : seller?.nickname }}</span></div>
                      <div class="flex justify-between"><span>下单时间</span> <span>{{ new Date(order.createTime).toLocaleString() }}</span></div>
                      <div v-if="order.payTime" class="flex justify-between"><span>付款时间</span> <span>{{ new Date(order.payTime).toLocaleString() }}</span></div>
                      <div v-if="order.shipTime" class="flex justify-between"><span>发货时间</span> <span>{{ new Date(order.shipTime).toLocaleString() }}</span></div>
                      <div v-if="order.completeTime" class="flex justify-between"><span>收货/完成时间</span> <span>{{ new Date(order.completeTime).toLocaleString() }}</span></div>
                  </div>
              </el-collapse-transition>
          </div>
      </el-card>
    </div>
    <el-empty v-else-if="!loading" description="订单不存在" />

    <!-- 底部操作栏 -->
    <div v-if="order" 
         class="fixed bottom-0 bg-white border-t border-gray-200 h-16 flex items-center justify-between px-4 z-50 transition-all duration-300"
         :style="{ left: isAdminRoute ? '200px' : '0', width: isAdminRoute ? 'calc(100% - 200px)' : '100%' }">
        
        <!-- 管理员视角的联系按钮 -->
        <div v-if="isAdminRoute" class="flex items-center space-x-4">
            <button class="flex flex-col items-center text-gray-600" @click="adminContactUser(order.sellerId, '卖家')">
                <el-icon :size="22"><ChatDotRound /></el-icon>
                <span class="text-[10px] mt-0.5">联系卖家</span>
            </button>
            <button class="flex flex-col items-center text-gray-600" @click="adminContactUser(order.buyerId, '买家')">
                <el-icon :size="22"><ChatDotSquare /></el-icon>
                <span class="text-[10px] mt-0.5">联系买家</span>
            </button>
        </div>

        <!-- 买家/卖家视角的联系按钮 -->
        <button v-else class="flex flex-col items-center text-gray-600" @click="startChat">
            <el-icon :size="24"><ChatDotRound /></el-icon>
            <span class="text-xs mt-1">{{ isSellerView ? '联系买家' : '联系卖家' }}</span>
        </button>

        <div class="flex items-center space-x-2">
            <!-- 0. 管理员视角专属操作 -->
            <template v-if="isAdminRoute">
                <!-- 维权中状态 -->
                <template v-if="order.disputeInProgress === 1">
                    <el-button type="danger" plain size="small" @click="handleAdminDispute('close_refund')">驳回维权</el-button>
                    <el-button type="success" size="small" @click="handleAdminDispute('force_refund')">强制退款</el-button>
                </template>
                <!-- 待发货、待收货、待评价、交易成功状态 -->
                <template v-if="[1, 2, 3, 4].includes(order.status)">
                    <el-button type="primary" size="small" @click="viewReviews">查看评价</el-button>
                </template>
            </template>

            <!-- 1. 买家视角按钮 -->
            <template v-if="!isAdminRoute && !isSellerView">
                <el-button v-if="order.status === 0" type="primary" @click="handlePay">去付款</el-button>
                <el-button v-if="order.status === 2" type="primary" @click="handleConfirmReceipt">确认收货</el-button>
                <el-button v-if="order.status === 3 && !hasBuyerReviewed" type="primary" @click="reviewDialogVisible = true">去评价</el-button>
                <el-button v-if="hasBuyerReviewed || order.status === 4" type="primary" @click="viewReviews">查看评价</el-button>
                <!-- 退款/维权操作 -->
                <el-button v-if="order.status === 5" type="primary" @click="handleCancelRefund">撤销退款</el-button>
                <el-button v-if="order.status === 8 && ![1, 2].includes(order.disputeInProgress)" plain @click="handleApplyDispute">我要维权</el-button>
            </template>

            <!-- 2. 卖家视角按钮 -->
            <template v-else-if="!isAdminRoute && isSellerView">
                <el-button v-if="order.status === 1" type="primary" @click="handleConfirmShipment">确认发货</el-button>
                <el-button v-if="order.status === 3 || order.status === 4" type="primary" @click="viewReviews">查看评价</el-button>
                <!-- 待处理退款 -->
                <el-button v-if="order.status === 5" plain @click="rejectRefundDialogVisible = true">拒绝退款</el-button>
                <el-button v-if="order.status === 5" type="primary" @click="handleAgreeRefund">同意退款</el-button>
            </template>
            
            <el-popover v-if="!isAdminRoute && !isSellerView && [0, 1, 2].includes(order.status)" placement="top-end" :width="100" trigger="click">
                <template #reference>
                    <el-button plain>更多</el-button>
                </template>
                <div class="flex flex-col space-y-2 text-center">
                    <button v-if="order.status === 0" @click="handleCancelOrder" class="hover:text-orange-500 text-sm">取消订单</button>
                    <button v-if="[1, 2].includes(order.status)" @click="refundDialogVisible = true" class="hover:text-orange-500 text-sm">我要退款</button>
                </div>
            </el-popover>
        </div>
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="发表评价" width="90%" destroy-on-close>
        <el-form :model="reviewForm" label-position="top">
            <el-form-item label="评分">
                <el-rate v-model="reviewForm.rating" :colors="['#F56C6C', '#E6A23C', '#FF9900']" />
            </el-form-item>
            <el-form-item label="评价内容">
                <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="说说你的使用心得吧～"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="reviewDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmitReview">提交评价</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 退款申请弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="90%" destroy-on-close>
        <el-form :model="refundForm" label-position="top">
            <el-form-item label="退款原因" required>
                <el-select v-model="refundForm.reason" placeholder="请选择退款原因" class="w-full">
                    <el-option label="不想要了" value="不想要了" />
                    <el-option label="商品信息描述不符" value="商品信息描述不符" />
                    <el-option label="商品损坏" value="商品损坏" />
                    <el-option label="其他" value="其他" />
                </el-select>
            </el-form-item>
            <el-form-item label="退款说明">
                <el-input v-model="refundForm.description" type="textarea" :rows="4" placeholder="请详细描述您的问题" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="refundDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleApplyRefund">提交申请</el-button>
            </span>
        </template>
    </el-dialog>

    <!-- 卖家拒绝退款弹窗 -->
    <el-dialog v-model="rejectRefundDialogVisible" title="拒绝退款" width="90%" destroy-on-close>
        <el-form :model="rejectRefundForm" label-position="top">
            <el-form-item label="拒绝原因" required>
                <el-input v-model="rejectRefundForm.reason" type="textarea" :rows="4" placeholder="请说明拒绝理由，以便买家了解情况" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="rejectRefundDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleRejectRefund">确认拒绝</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../stores/user.store';
import { useAuthStore } from '../../stores/auth.store';
import apiClient from '../../services/api';
import { ArrowLeftBold, ArrowRightBold, ChatDotRound, ChatDotSquare, Check } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();

const order = ref(null);
const product = ref(null);
const buyer = ref(null);
const seller = ref(null);
const reviews = ref([]);
const loading = ref(true);

const reviewDialogVisible = ref(false);
const reviewForm = reactive({ rating: 5, comment: '' });

const refundDialogVisible = ref(false);
const refundForm = reactive({ reason: '', description: '' });

const rejectRefundDialogVisible = ref(false);
const rejectRefundForm = reactive({ reason: '' });

const getRefundHistoryText = (text) => {
    if (!text) return '';
    if (isAdminRoute.value) {
        // 管理员视角转换
        return text
            .replace('您发起了退款申请，等待卖家处理。', '买家发起了退款申请，等待卖家处理。')
            .replace('您重新发起了退款申请，等待卖家处理。', '买家重新发起了退款申请，等待卖家处理。')
            .replace('卖家已同意退款，资金已退回到您的余额。', '卖家已同意退款，资金已退回到买家的余额。')
            .replace('卖家拒绝了您的退款申请。', '卖家拒绝了买家的退款申请。');
    } else if (isSellerView.value) {
        // 卖家视角转换
        return text
            .replace('您发起了退款申请，等待卖家处理。', '买家发起了退款申请，等待您处理。')
            .replace('您重新发起了退款申请，等待卖家处理。', '买家重新发起了退款申请，等待您处理。')
            .replace('卖家已同意退款，资金已退回到您的余额。', '您已同意退款，资金已退回到买家的余额。')
            .replace('卖家拒绝了您的退款申请。', '您拒绝了买家的退款申请。');
    } else {
        // 买家视角保持原样
        return text;
    }
};

const hasBuyerReviewed = computed(() => {
    return reviews.value.some(r => r.reviewerId === authStore.user?.id && r.isAdditional === 0);
});

const isAdminRoute = computed(() => route.path.startsWith('/admin'));

const fetchOrderDetail = async () => {
    const orderId = route.params.id;
    loading.value = true;
    try {
        const response = await apiClient.get(`/orders/${orderId}`);
        const data = response.data;
        // 整合退款详情数据
        order.value = {
            ...data.order,
            refundInfo: data.refundInfo,
            disputeInProgress: data.refundInfo?.refund?.disputeInProgress || 0 // 显式同步维权状态
        };
        product.value = data.product;
        buyer.value = data.buyer;
        seller.value = data.seller;
        reviews.value = data.reviews || [];
    } catch (error) {
        console.error('获取订单详情失败:', error);
        ElMessage.error('无法拉取数据，请检查网络');
    } finally {
        loading.value = false;
    }
};

onMounted(fetchOrderDetail);

const sections = reactive({
    timeline: { expanded: true },
    price: { expanded: false },
    info: { expanded: false },
});

const isSellerView = computed(() => order.value?.sellerId === authStore.user?.id);

const displayStatus = computed(() => {
    if (!order.value) return '';
    const statusMap = {
        0: '待付款', 1: '待发货', 2: '待收货', 3: '待评价', 
        4: '交易成功', 5: '退款中', 6: '已取消', 7: '退款成功', 8: '退款失败'
    };
    return statusMap[order.value.status] || '未知状态';
});

const timelineActivities = computed(() => {
    if (!order.value) return [];
    const steps = ['拍下', '付款', '发货', '收货', '评价'];
    const statusValue = Number(order.value.status);
    
    let currentStepIndex = 0;
    if (statusValue === 0) currentStepIndex = 1; 
    else if (statusValue === 1) currentStepIndex = 2; 
    else if (statusValue === 2) currentStepIndex = 3; 
    else if (statusValue >= 3) currentStepIndex = 4; 

    return steps.map((step, index) => {
        let content = index < currentStepIndex ? `已${step}` : `待${step}`;
        if (index === currentStepIndex) content = displayStatus.value;
        if (index === 4 && statusValue === 4) content = '已评价';

        return {
            content,
            hollow: index > currentStepIndex,
            type: index <= currentStepIndex ? 'primary' : 'info',
            icon: index === currentStepIndex ? Check : null,
        };
    });
});

const handlePay = () => {
    ElMessageBox.confirm(`确认支付 ¥${parseFloat(order.value.totalPrice).toFixed(2)}`, '支付确认', { type: 'info' })
    .then(async () => {
        try {
            await apiClient.put(`/orders/${order.value.id}/status`, { status: 1 });
            ElMessage.success('支付成功！');
            fetchOrderDetail();
        } catch (error) { ElMessage.error('支付失败'); }
    }).catch(() => {});
};

const handleConfirmShipment = () => {
    ElMessageBox.confirm('确认已发货？', '操作确认', { type: 'warning' })
    .then(async () => {
        try {
            await apiClient.put(`/orders/${order.value.id}/status`, { status: 2 });
            ElMessage.success('发货成功');
            fetchOrderDetail();
        } catch (error) { ElMessage.error('操作失败'); }
    }).catch(() => {});
};

const handleConfirmReceipt = () => {
    ElMessageBox.confirm('收到宝贝了吗？确认后将结算资金给卖家。', '确认收货', { type: 'warning' })
    .then(async () => {
        try {
            await apiClient.put(`/orders/${order.value.id}/status`, { status: 3 });
            ElMessage.success('收货成功！');
            fetchOrderDetail();
        } catch (error) { ElMessage.error('操作失败'); }
    }).catch(() => {});
};

const handleApplyRefund = async () => {
    if (!refundForm.reason) {
        ElMessage.warning('请选择退款原因');
        return;
    }
    try {
        await apiClient.post(`/orders/${order.value.id}/refund`, {
            reason: refundForm.reason,
            description: refundForm.description
        });
        ElMessage.success('退款申请已提交');
        refundDialogVisible.value = false;
        fetchOrderDetail();
    } catch (error) {
        ElMessage.error(error.response?.data || '申请失败');
    }
};

const handleAgreeRefund = () => {
    ElMessageBox.confirm('确定同意买家的退款申请吗？同意后订单金额将退回买家余额。', '提示', { type: 'warning' })
    .then(async () => {
        try {
            await apiClient.put(`/orders/${order.value.id}/refund/handle`, { action: 'agree' });
            ElMessage.success('已同意退款');
            fetchOrderDetail();
        } catch (error) {
            ElMessage.error('操作失败');
        }
    }).catch(() => {});
};

const handleRejectRefund = async () => {
    if (!rejectRefundForm.reason.trim()) {
        ElMessage.warning('请填写拒绝原因');
        return;
    }
    try {
        await apiClient.put(`/orders/${order.value.id}/refund/handle`, {
            action: 'reject',
            reason: rejectRefundForm.reason
        });
        ElMessage.success('已拒绝退款申请');
        rejectRefundDialogVisible.value = false;
        fetchOrderDetail();
    } catch (error) {
        ElMessage.error('操作失败');
    }
};

const handleSubmitReview = async () => {
    if (!reviewForm.comment.trim()) {
        ElMessage.warning('请填写评价内容');
        return;
    }
    try {
        await apiClient.post(`/orders/${order.value.id}/review`, {
            rating: reviewForm.rating,
            comment: reviewForm.comment,
            isAdditional: 0
        });
        ElMessage.success('评价成功！');
        reviewDialogVisible.value = false;
        fetchOrderDetail();
    } catch (error) { ElMessage.error('评价提交失败'); }
};

const viewReviews = () => {
    router.push(`/user/order/${order.value.id}/review`);
};

const handleCancelOrder = () => {
    ElMessageBox.confirm('确定取消订单吗？', '提示', { type: 'warning' })
    .then(async () => {
        try {
            await apiClient.put(`/orders/${order.value.id}/status`, { status: 6 });
            ElMessage.success('订单已取消');
            fetchOrderDetail();
        } catch (error) { ElMessage.error('操作失败'); }
    }).catch(() => {});
};

const handleApplyDispute = () => {
    ElMessageBox.confirm('确定要申请平台维权吗？管理员将介入处理。', '申请维权', { type: 'warning' })
    .then(async () => {
        try {
            await apiClient.post(`/orders/${order.value.id}/refund/dispute`);
            ElMessage.success('维权申请已提交，请等待管理员处理');
            fetchOrderDetail();
        } catch (error) {
            ElMessage.error(error.response?.data || '申请失败');
        }
    }).catch(() => {});
};

const handleCancelRefund = () => {
    ElMessageBox.confirm('确定要撤销退款申请吗？撤销后订单将恢复正常流程。', '撤销退款', { type: 'warning' })
    .then(async () => {
        try {
            // 撤销逻辑：将状态改回待收货(2)或待发货(1)
            // 这里简单处理为恢复到申请前的状态，或者根据是否有 shipTime 判定
            const originalStatus = order.value.shipTime ? 2 : 1;
            await apiClient.put(`/orders/${order.value.id}/status`, { status: originalStatus });
            ElMessage.success('退款申请已撤销');
            fetchOrderDetail();
        } catch (error) {
            ElMessage.error('撤销失败');
        }
    }).catch(() => {});
};

// 管理员联系用户逻辑
const adminContactUser = async (userId, label) => {
    try {
        const conv = await userStore.getOrCreateConversation(userId);
        router.push({
            path: `/chat/${conv.id}`,
            query: { orderId: order.value.id }
        });
    } catch (error) {
        ElMessage.error(`无法联系${label}`);
    }
};

// 管理员处理维权逻辑
const handleAdminDispute = (action) => {
    const actionText = action === 'force_refund' ? '强制退款' : '驳回维权';
    const confirmMessage = action === 'force_refund' 
        ? `确定要执行强制退款吗？资金将退回到买家余额。` 
        : `确定要驳回该维权申请吗？`;

    ElMessageBox.confirm(confirmMessage, '维权处理', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: action === 'force_refund' ? 'warning' : 'info'
    }).then(async () => {
        try {
            await apiClient.put(`/admin/orders/${order.value.id}/refund/handle`, { action });
            ElMessage.success(`${actionText}处理成功`);
            fetchOrderDetail(); // 刷新详情
        } catch (error) {
            console.error('维权处理失败:', error);
            ElMessage.error(error.response?.data?.message || '操作失败');
        }
    }).catch(() => {});
};

const startChat = () => {
    const targetId = isSellerView.value ? order.value.buyerId : order.value.sellerId;
    userStore.getOrCreateConversation(targetId).then(conv => {
        router.push(`/chat/${conv.id}`);
    });
};
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
    border-color: #f97316 !important;
}
:deep(.el-timeline-item__tail) {
    border-left: 2px solid #f97316 !important;
}
.admin-bottom-bar {
    left: 200px !important;
    width: calc(100% - 200px) !important;
}
</style>
