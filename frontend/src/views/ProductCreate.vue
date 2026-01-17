<template>
  <div class="container mx-auto p-4 sm:p-6 lg:p-8">
    <h1 class="text-2xl font-bold text-gray-800 mb-6">发布您的闲置宝贝</h1>
    <el-card class="max-w-2xl mx-auto">
      <el-form :model="form" label-position="top" class="space-y-4">
        <el-form-item label="商品标题">
          <el-input v-model="form.title" placeholder="请输入商品标题" size="large"></el-input>
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="详细描述一下您的宝贝吧..."
          ></el-input>
        </el-form-item>

        <el-form-item label="商品分类">
          <!-- 分类功能后续实现，暂时用一个输入框代替 -->
          <el-input v-model.number="form.categoryId" placeholder="请输入分类ID (暂用)" size="large"></el-input>
        </el-form-item>

        <el-form-item label="商品售价 (元)">
          <el-input-number v-model="form.price" :precision="2" :step="1" :min="0" size="large" class="w-full"></el-input-number>
        </el-form-item>

        <div class="pt-4">
          <el-button type="primary" @click="handleSubmit" class="w-full" size="large" :loading="loading">确认发布</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import ProductService from '../services/product.service';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);

// 表单数据
const form = reactive({
  title: '',
  description: '',
  categoryId: null,
  price: 0.00,
});

// 处理发布逻辑
const handleSubmit = () => {
  if (!form.title || !form.price || !form.categoryId) {
    ElMessage.error('标题、分类和价格不能为空！');
    return;
  }
  loading.value = true;
  ProductService.createProduct(form)
    .then(() => {
      ElMessage.success('商品发布成功！');
      router.push('/'); // 发布成功后跳转到首页
    })
    .catch(error => {
      console.error('商品发布失败:', error);
      const errMsg = error.response?.data?.message || '发布失败，请稍后再试。';
      ElMessage.error(errMsg);
    })
    .finally(() => {
      loading.value = false;
    });
};
</script>

