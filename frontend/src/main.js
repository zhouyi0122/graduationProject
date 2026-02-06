import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';

// 1. 移除默认的CSS导入
// import 'element-plus/dist/index.css';

// 2. 导入我们自定义的SCSS主题文件
import './styles/element-theme.scss';

import './style.css';
import App from './App.vue';
import router from './router';

// 引入中文语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

const app = createApp(App);

app.use(createPinia());
app.use(router);

// 全局配置Element Plus，并设置中文
app.use(ElementPlus, { locale: zhCn });

app.mount('#app');
