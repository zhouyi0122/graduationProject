import { defineStore } from 'pinia';
import apiClient from '../services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')),
    returnUrl: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.user,
    isAdmin: (state) => state.user && state.user.roles && state.user.roles.includes('ROLE_ADMIN'),
  },
  actions: {
    async login(username, password) {
      try {
        const response = await apiClient.post('/auth/login', { username, password });
        const user = response.data;
        
        this.user = user;
        localStorage.setItem('user', JSON.stringify(user));
        
        return user.roles && user.roles.includes('ROLE_ADMIN');
      } catch (error) {
        throw error.response?.data || new Error('登录请求失败');
      }
    },
    async register(userData) {
      try {
        const response = await apiClient.post('/auth/register', userData);
        return response.data;
      } catch (error) {
        throw error.response?.data || new Error('注册请求失败');
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem('user');
    }
  }
});
