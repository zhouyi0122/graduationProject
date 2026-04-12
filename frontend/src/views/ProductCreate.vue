<template>
  <div class="product-create-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">{{ isEditMode ? '编辑您的闲置宝贝' : '发布您的闲置宝贝' }}</h1>
      </div>
    </header>

    <div class="p-4">
        <el-card class="max-w-2xl mx-auto" v-loading="loading">
          <el-form :model="form" label-position="top" class="space-y-6">
            
            <div>
                <h2 class="text-base font-semibold text-gray-700 mb-2">上传图片</h2>
                <el-form-item label="第一张图片将作为商品封面">
                  <el-upload
                    v-model:file-list="fileList"
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    list-type="picture-card"
                    :on-preview="handlePictureCardPreview"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    multiple
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
            </div>

            <el-divider />

            <div>
                <h2 class="text-base font-semibold text-gray-700 mb-2">基本信息</h2>
                <el-form-item label="商品标题">
                  <el-input v-model="form.title" placeholder="请输入商品标题" size="large"></el-input>
                </el-form-item>

                <el-form-item label="新旧程度">
                  <el-select v-model="form.condition" placeholder="请选择新旧程度" size="large" class="w-full">
                    <el-option label="全新" value="全新"></el-option>
                    <el-option label="九成新" value="九成新"></el-option>
                    <el-option label="八成新" value="八成新"></el-option>
                    <el-option label="七成新及以下" value="七成新及以下"></el-option>
                  </el-select>
                </el-form-item>

                <div>
                    <div class="flex items-center justify-between mb-2">
                        <label class="el-form-item__label" style="margin-bottom: 0;">商品描述</label>
                        <button type="button" @click="handleAiGenerate" :disabled="aiLoading" class="flex items-center text-sm bg-orange-100 text-orange-600 font-semibold px-2 py-1 rounded-md transition-colors hover:bg-orange-200 disabled:opacity-60 disabled:cursor-not-allowed">
                            <el-icon class="mr-1" :size="16"><MagicStick /></el-icon>
                            {{ aiLoading ? 'AI生成中...' : 'AI帮你写' }}
                        </button>
                    </div>
                    <el-input
                      v-model="form.description"
                      type="textarea"
                      :rows="5"
                      placeholder="详细描述一下您的宝贝吧..."
                    />
                </div>
            </div>

            <el-divider />

            <div>
                <h2 class="text-base font-semibold text-gray-700 mb-2">价格</h2>
                <el-form-item label="商品售价 (元)">
                  <el-input-number v-model="form.price" :precision="2" :step="1" :min="0" size="large" class="w-full"></el-input-number>
                </el-form-item>
                <el-alert
                  v-if="aiSuggestion.reason"
                  :title="`AI建议价：${aiSuggestion.suggestedPrice || 0} 元（区间 ${aiSuggestion.priceMin || 0} ~ ${aiSuggestion.priceMax || 0}）`"
                  :description="aiSuggestion.reason"
                  type="info"
                  :closable="false"
                  show-icon
                />
            </div>

            <div class="pt-4 flex space-x-4">
              <el-button @click="handleSaveDraft" class="w-1/2" size="large">存为草稿</el-button>
              <el-button type="primary" @click="handleSubmit" class="w-1/2" size="large" :loading="loading">
                {{ isEditMode ? '确认修改' : '确认发布' }}
              </el-button>
            </div>
          </el-form>
        </el-card>
    </div>

    <el-dialog v-model="dialogVisible">
      <img class="w-full" :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import { useProductStore } from '../stores/product.store';
import apiClient from '../services/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, MagicStick, ArrowLeftBold } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const productStore = useProductStore();

const loading = ref(false);
const aiLoading = ref(false);
const isEditMode = computed(() => !!route.params.id);
const draftId = computed(() => route.query.draftId); // 新增：识别草稿 ID
let isNavigating = false;

const aiSuggestion = reactive({
  priceMin: 0,
  priceMax: 0,
  suggestedPrice: 0,
  reason: '',
});

const form = reactive({
  title: '',
  description: '',
  price: 0.00,
  condition: '',
  categoryId: 1,
});

const fileList = ref([]);
const dialogImageUrl = ref('');
const dialogVisible = ref(false);

const uploadUrl = 'http://localhost:8080/api/upload';
const uploadHeaders = computed(() => {
  const user = JSON.parse(localStorage.getItem('user'));
  return user && user.token ? { Authorization: 'Bearer ' + user.token } : {};
});

const fetchProductData = async () => {
  // 优先级：正式商品编辑 > 草稿继续编辑
  if (isEditMode.value) {
    loading.value = true;
    try {
      const data = await productStore.fetchProductById(route.params.id);
      form.title = data.title;
      form.description = data.description;
      form.price = data.price;
      form.condition = data.condition;
      form.categoryId = data.categoryId || 1;
      
      // 修改：从 images 列表回显多张图片
      if (data.images && data.images.length > 0) {
        fileList.value = data.images.map(img => ({
          url: img.imageUrl,
          name: img.imageUrl.substring(img.imageUrl.lastIndexOf('/') + 1)
        }));
      } else if (data.imageUrl) {
        fileList.value = [{ url: data.imageUrl, name: 'current_image' }];
      }
    } catch (error) {
      console.error('获取商品详情失败:', error);
      ElMessage.error('无法加载商品数据');
    } finally {
      loading.value = false;
    }
  } else if (draftId.value) {
    // 处理草稿回显
    loading.value = true;
    try {
      // 从 store 中寻找当前草稿（或直接从后端获取，这里先假设 store 已有）
      if (userStore.drafts.length === 0) await userStore.fetchDrafts();
      const draft = userStore.drafts.find(d => String(d.id) === String(draftId.value));
      if (draft) {
        form.title = draft.title || '';
        form.description = draft.description || '';
        form.price = draft.price || 0;
        form.condition = draft.condition || '';
        form.categoryId = draft.categoryId || 1;
        if (draft.imageUrls) {
          fileList.value = draft.imageUrls.split(',').filter(url => url).map(url => ({ url }));
        }
      }
    } catch (error) {
      ElMessage.error('加载草稿失败');
    } finally {
      loading.value = false;
    }
  }
};

onMounted(() => {
  fetchProductData();
});

const isFormEmpty = computed(() => {
    return !form.title.trim() && !form.description.trim() && !form.condition && fileList.value.length === 0;
});

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url;
  dialogVisible.value = true;
};

const handleSaveDraft = async () => {
    if (isFormEmpty.value) {
        ElMessage.warning('草稿内容不能为空！');
        return;
    }
    try {
        const images = fileList.value.map(f => f.response?.url || f.url).filter(url => url && !url.startsWith('blob:'));
        const draftData = { 
            ...form, 
            images: images
        };
        // 如果是从已有草稿进入的，带上草稿 ID 以便后端更新
        if (draftId.value) {
            draftData.id = draftId.value;
        }
        
        await userStore.saveProductDraft(draftData);
        ElMessage.success('草稿已保存到云端！');
        isNavigating = true;
        router.push('/user/products?tab=drafts');
    } catch (error) {
        console.error('保存草稿失败:', error);
        ElMessage.error('保存草稿失败，请稍后再试');
    }
}

const handleAiGenerate = async () => {
  const imageUrls = fileList.value.map(f => f.response?.url || f.url).filter(url => !!url && !url.startsWith('blob:'));
  if (imageUrls.length === 0) {
    ElMessage.warning('请先上传至少一张商品图片再使用AI');
    return;
  }

  aiLoading.value = true;
  try {
    const response = await apiClient.post('/ai/product-assistant', {
      title: form.title,
      condition: form.condition,
      imageUrls,
    });

    const data = response.data || {};
    const hasManualTitle = !!form.title;
    const hasManualCondition = !!form.condition;

    if (!hasManualTitle && data.title) {
      form.title = data.title;
    }
    if (!hasManualCondition && data.condition) {
      form.condition = data.condition;
    }
    if (data.description) {
      form.description = data.description;
    }

    aiSuggestion.priceMin = Number(data.priceMin || 0);
    aiSuggestion.priceMax = Number(data.priceMax || 0);
    aiSuggestion.suggestedPrice = Number(data.suggestedPrice || 0);
    aiSuggestion.reason = data.reason || '';

    if (aiSuggestion.suggestedPrice > 0) {
      form.price = aiSuggestion.suggestedPrice;
    }

    ElMessage.success('AI生成完成，已保留您的手动输入，仅补全其余字段');
  } catch (error) {
    console.error('AI生成失败:', error);
    ElMessage.error(error.response?.data || 'AI生成失败，请稍后再试');
  } finally {
    aiLoading.value = false;
  }
};

const handleSubmit = async () => {
  if (!form.title || !form.price || !form.condition || (fileList.value.length === 0 && !isEditMode.value)) {
    ElMessage.error('图片、标题、新旧程度和价格不能为空！');
    return;
  }
  loading.value = true;
  
  try {
    const productData = { 
      ...form, 
      images: fileList.value.map(f => f.response?.url || f.url).filter(url => url && !url.startsWith('blob:')) 
    };
    
    if (isEditMode.value) {
      await apiClient.put(`/products/${route.params.id}`, productData);
      ElMessage.success('商品更新成功！');
      isNavigating = true;
      router.back(); // 编辑成功返回上一页
    } else {
      await apiClient.post('/products', productData);
      ElMessage.success('商品发布成功！');
      isNavigating = true;
      router.push('/user/products'); // 发布成功跳转到我的发布
    }
    
    await productStore.fetchProducts();
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error(isEditMode.value ? '更新失败' : '发布失败');
  } finally {
    loading.value = false;
  }
};

onBeforeRouteLeave((to, from, next) => {
  if (!isFormEmpty.value && !isNavigating) {
    ElMessageBox.confirm(
      '您编辑的内容尚未保存，是否存为草稿？',
      '提示',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '存为草稿',
        cancelButtonText: '直接离开',
        type: 'warning',
      }
    )
      .then(() => {
        handleSaveDraft();
      })
      .catch((action) => {
        if (action === 'cancel') {
          next();
        } else {
          next(false);
        }
      });
  } else {
    next();
  }
});
</script>

<style scoped>
:deep(.el-select__wrapper.is-focused) {
    box-shadow: 0 0 0 1px #f97316 inset !important;
}
:deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px #f97316 inset !important;
}
</style>
