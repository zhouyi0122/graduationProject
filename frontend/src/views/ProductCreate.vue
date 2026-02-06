<template>
  <div class="product-create-page bg-gray-100 min-h-screen">
    <!-- Top Fixed Header -->
    <header class="sticky top-0 z-40 bg-white shadow-sm">
      <div class="container mx-auto px-2 py-2 flex items-center relative h-14">
        <button @click="router.back()" class="absolute left-2 p-2 text-gray-600 hover:text-gray-900">
          <el-icon :size="20"><ArrowLeftBold /></el-icon>
        </button>
        <h1 class="text-lg font-semibold text-gray-800 text-center w-full">发布您的闲置宝贝</h1>
      </div>
    </header>

    <div class="p-4">
        <el-card class="max-w-2xl mx-auto">
          <el-form :model="form" label-position="top" class="space-y-6">
            
            <div>
                <h2 class="text-base font-semibold text-gray-700 mb-2">上传图片</h2>
                <el-form-item label="第一张图片将作为商品封面">
                  <el-upload
                    v-model:file-list="fileList"
                    action="#"
                    list-type="picture-card"
                    :auto-upload="false"
                    :on-preview="handlePictureCardPreview"
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
                        <button class="flex items-center text-sm bg-orange-100 text-orange-600 font-semibold px-2 py-1 rounded-md transition-colors hover:bg-orange-200">
                            <el-icon class="mr-1" :size="16"><MagicStick /></el-icon>
                            AI帮你写
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
            </div>

            <div class="pt-4 flex space-x-4">
              <el-button @click="handleSaveDraft" class="w-1/2" size="large">存为草稿</el-button>
              <el-button type="primary" @click="handleSubmit" class="w-1/2" size="large" :loading="loading">确认发布</el-button>
            </div>
          </el-form>
        </el-card>
    </div>

    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { useUserStore } from '../stores/user.store';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, MagicStick, ArrowLeftBold } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
let isNavigating = false; // Flag to prevent multiple dialogs

const form = reactive({
  title: '',
  description: '',
  price: 0.00,
  condition: '',
});

const fileList = ref([]);
const dialogImageUrl = ref('');
const dialogVisible = ref(false);

// Computed property to check if the form is empty
const isFormEmpty = computed(() => {
    return !form.title.trim() && !form.description.trim() && !form.condition && fileList.value.length === 0;
});

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url;
  dialogVisible.value = true;
};

// Logic to save draft
const handleSaveDraft = () => {
    if (isFormEmpty.value) {
        ElMessage.warning('草稿内容不能为空！');
        return;
    }
    const draftData = { ...form, images: fileList.value.map(f => f.url) };
    userStore.saveDraft(draftData);
    ElMessage.success('草稿已保存！');
    isNavigating = true; // Allow navigation after saving
    router.push('/user/products?tab=drafts');
}

// Logic to submit product
const handleSubmit = () => {
  if (!form.title || !form.price || !form.condition || fileList.value.length === 0) {
    ElMessage.error('图片、标题、新旧程度和价格不能为空！');
    return;
  }
  loading.value = true;
  const productData = { ...form, images: fileList.value.map(f => f.url) };
  
  // Simulating an API call
  setTimeout(() => {
      userStore.publishProduct(productData);
      ElMessage.success('商品发布成功！');
      isNavigating = true; // Allow navigation after successful submission
      router.push('/user/products');
      loading.value = false;
  }, 1000);
};

// Navigation guard
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
        // Navigation will be handled inside handleSaveDraft, so we don't call next() here.
      })
      .catch((action) => {
        if (action === 'cancel') {
          next(); // Allow navigation
        } else {
          next(false); // Stay on the page
        }
      });
  } else {
    next(); // Allow navigation
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
