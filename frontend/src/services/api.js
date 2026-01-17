import axios from 'axios';
import { useAuthStore } from '../stores/auth.store';

// 创建一个axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API的基础路径
  headers: {
    'Content-Type': 'application/json',
  },
});

// 添加请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const user = authStore.user;

    if (user && user.token) {
      config.headers.Authorization = 'Bearer ' + user.token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;
