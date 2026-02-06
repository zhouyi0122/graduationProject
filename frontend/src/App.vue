<template>
  <el-config-provider :locale="zhCn">
    <div id="app" class="bg-gray-100 font-sans">
      <!-- 主内容区域 -->
      <!-- 根据是否显示底部导航栏，动态调整padding-bottom -->
      <main :class="{ 'pb-20': showBottomNav }">
        <router-view />
      </main>

      <!-- 底部导航栏 -->
      <BottomNav v-if="showBottomNav" />
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from './stores/auth.store';
import BottomNav from './components/BottomNav.vue';

// 引入中文语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

const authStore = useAuthStore();
const route = useRoute();

// 计算是否应该显示底部导航栏
const showBottomNav = computed(() => {
  if (!authStore.isLoggedIn) return false;
  // 定义需要显示底部导航栏的一级页面路径
  const mainPages = ['/', '/chat', '/user'];
  return mainPages.includes(route.path);
});

</script>

<style>
/* 全局样式 */
body {
  margin: 0;
}
</style>
