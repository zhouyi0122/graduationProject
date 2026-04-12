<template>
  <div class="product-management-page">
    <h1 class="text-2xl font-bold mb-4">商品管理</h1>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="商品列表" name="products">
        <el-card v-loading="loadingProducts">
          <div class="mb-4 flex items-center gap-2">
            <el-select v-model="productStatusFilter" placeholder="按商品状态筛选" style="width: 200px" @change="loadProducts">
              <el-option label="全部" value="" />
              <el-option label="在售" :value="0" />
              <el-option label="已售出" :value="1" />
              <el-option label="已下架" :value="2" />
            </el-select>
          </div>

          <el-table :data="productStore.products" style="width: 100%" @row-click="viewProductDetail" class="cursor-pointer">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
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
            <el-table-column label="审核状态" width="140">
              <template #default="scope">
                <el-tag v-if="scope.row.aiReviewStatus === null" type="info">未审核</el-tag>
                <el-tag v-else-if="scope.row.aiReviewStatus === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.aiReviewStatus === 1" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已下架</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240">
              <template #default="scope">
                <el-button
                  size="small"
                  :type="scope.row.status === 0 ? 'danger' : 'success'"
                  :disabled="scope.row.status === 1"
                  @click.stop="() => handleToggleStatus(scope.row)"
                >
                  {{ scope.row.status === 0 ? '强制下架' : '重新上架' }}
                </el-button>
                <el-tooltip :content="getAiReviewDisabledReason(scope.row)" placement="top" :disabled="canAiReview(scope.row)">
                  <span>
                    <el-button
                      size="small"
                      type="warning"
                      :loading="aiReviewingProductId === scope.row.id"
                      :disabled="aiReviewingProductId !== null || !canAiReview(scope.row)"
                      :class="{ 'ai-review-disabled-btn': aiReviewingProductId !== null || !canAiReview(scope.row) }"
                      @click.stop="() => handleRunAiReview(scope.row)"
                    >
                      {{ aiReviewingProductId === scope.row.id ? 'AI审核中...' : 'AI审核' }}
                    </el-button>
                  </span>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="AI智能审核" name="aiReviews">
        <el-card>
          <div class="mb-4 flex items-center gap-2">
            <el-select v-model="aiStatusFilter" placeholder="按状态筛选" style="width: 200px" @change="loadAiReviews">
              <el-option label="全部" value="" />
              <el-option label="待人工审核" :value="0" />
              <el-option label="人工通过" :value="1" />
              <el-option label="人工下架" :value="2" />
            </el-select>
          </div>

          <el-table v-loading="loadingAiReviews" :data="productStore.aiReviews" style="width: 100%">
            <el-table-column prop="productId" label="商品ID" width="90" />
            <el-table-column prop="productTitle" label="商品标题" min-width="180" show-overflow-tooltip />
            <el-table-column label="卖家" min-width="140">
              <template #default="scope">
                {{ scope.row.sellerNickname || scope.row.sellerUsername || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="productPrice" label="价格" width="110">
              <template #default="scope">¥{{ scope.row.productPrice ?? '-' }}</template>
            </el-table-column>
            <el-table-column prop="riskScore" label="风险分(0-10)" width="120">
              <template #default="scope">
                <el-tag :type="getRiskType(scope.row.riskScore)">{{ scope.row.riskScore }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskTags" label="命中标签" min-width="180" show-overflow-tooltip />
            <el-table-column prop="aiReason" label="AI说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="审核状态" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已下架</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="success"
                  @click="handleApproveAiReview(scope.row)"
                >
                  通过
                </el-button>
                <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="danger"
                  @click="handleOffShelfByAiReview(scope.row)"
                >
                  下架
                </el-button>
                <el-button size="small" @click="viewProductById(scope.row.productId)">查看商品</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useProductStore } from '../../stores/product.store';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const productStore = useProductStore();

const activeTab = ref('products');
const loadingProducts = ref(false);
const loadingAiReviews = ref(false);
const aiReviewingProductId = ref(null);
const productStatusFilter = ref('');
const aiStatusFilter = ref('');

const getStatusLabel = (status) => {
  const map = { 0: '在售', 1: '已售出', 2: '已下架' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'info', 2: 'danger' };
  return map[status] || 'info';
};

const getRiskType = (score) => {
  const s = Number(score || 0);
  if (s >= 7) return 'danger';
  if (s >= 4) return 'warning';
  return 'success';
};

const loadProducts = async () => {
  loadingProducts.value = true;
  try {
    const status = productStatusFilter.value === '' ? undefined : productStatusFilter.value;
    await productStore.fetchAllAdminProducts(status);
  } catch (error) {
    ElMessage.error('加载商品列表失败');
  } finally {
    loadingProducts.value = false;
  }
};

const loadAiReviews = async () => {
  loadingAiReviews.value = true;
  try {
    const status = aiStatusFilter.value === '' ? undefined : aiStatusFilter.value;
    await productStore.fetchProductAiReviews(status);
  } catch (error) {
    ElMessage.error('加载AI审核列表失败');
  } finally {
    loadingAiReviews.value = false;
  }
};

onMounted(async () => {
  await Promise.all([loadProducts(), loadAiReviews()]);
});

watch(activeTab, async (tab) => {
  if (tab === 'products') {
    await loadProducts();
  }
});

const viewProductDetail = (row, column) => {
  if (column && column.label === '操作') return;
  router.push(`/product/${row.id}`);
};

const viewProductById = (id) => {
  router.push(`/product/${id}`);
};

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
    })
    .catch(() => {});
};

const canAiReview = (product) => {
  const status = Number(product.status);
  const aiReviewStatus = product.aiReviewStatus === null || product.aiReviewStatus === undefined
    ? null
    : Number(product.aiReviewStatus);
  return status === 0 && aiReviewStatus !== 1;
};

const getAiReviewDisabledReason = (product) => {
  const status = Number(product.status);
  const aiReviewStatus = product.aiReviewStatus === null || product.aiReviewStatus === undefined
    ? null
    : Number(product.aiReviewStatus);

  if (aiReviewStatus === 1) {
    return '已人工通过，无需重复审核';
  }
  if (status !== 0) {
    return '仅在售商品可进行AI审核';
  }
  return '';
};

const handleRunAiReview = async (product) => {
  if (!canAiReview(product) || aiReviewingProductId.value !== null) {
    return;
  }

  aiReviewingProductId.value = product.id;
  ElMessage.info('AI审核中，请稍候...');
  try {
    await productStore.runProductAiReview(product.id);
    await Promise.all([loadAiReviews(), loadProducts()]);
    activeTab.value = 'aiReviews';
    ElMessage.success('AI审核完成，已跳转至AI智能审核列表');
  } catch (error) {
    ElMessage.error(error.response?.data || 'AI审核失败，请稍后重试');
  } finally {
    aiReviewingProductId.value = null;
  }
};

const handleApproveAiReview = async (row) => {
  try {
    await productStore.approveProductAiReview(row.reviewId);
    ElMessage.success('已标记为人工通过');
    await loadAiReviews();
  } catch (error) {
    ElMessage.error(error.response?.data || '操作失败');
  }
};

const handleOffShelfByAiReview = async (row) => {
  try {
    await productStore.offShelfProductByAiReview(row.reviewId);
    ElMessage.success('商品已下架');
    await Promise.all([loadAiReviews(), loadProducts()]);
  } catch (error) {
    ElMessage.error(error.response?.data || '下架失败');
  }
};
</script>

<style scoped>
.ai-review-disabled-btn {
  opacity: 0.45 !important;
  cursor: not-allowed !important;
}
</style>
