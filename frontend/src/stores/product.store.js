import { defineStore } from 'pinia';
import apiClient from '../services/api';

export const useProductStore = defineStore('products', {
  state: () => ({
    products: [], 
    categories: [
        { id: 1, name: '电子产品', href: '#' },
        { id: 2, name: '学习书籍', href: '#' },
        { id: 3, name: '生活用品', href: '#' },
        { id: 4, name: '服饰鞋包', href: '#' },
        { id: 5, name: '运动户外', href: '#' },
    ]
  }),
  getters: {
    getProductById: (state) => (id) => {
      return state.products.find(p => p.id === parseInt(id));
    }
  },
  actions: {
    async fetchProducts(query = '', sort = 'latest') {
      try {
        let url = `/products?q=${encodeURIComponent(query)}&sort=${sort}`;
        const response = await apiClient.get(url);
        this.products = response.data;
      } catch (error) {
        console.error('获取商品列表失败:', error);
      }
    },
    // 获取单个商品详情
    async fetchProductById(id) {
      try {
        const response = await apiClient.get(`/products/${id}`);
        return response.data;
      } catch (error) {
        console.error('获取商品详情失败:', error);
        throw error;
      }
    },
    // 管理员：获取所有商品列表
    async fetchAllAdminProducts() {
      try {
        const response = await apiClient.get('/admin/products');
        this.products = response.data;
        return response.data;
      } catch (error) {
        console.error('管理员获取商品列表失败:', error);
        throw error;
      }
    },
    // 管理员：切换商品状态（下架/上架）
    async adminToggleProductStatus(productId) {
      try {
        const response = await apiClient.put(`/admin/products/${productId}/status`);
        // 同步更新本地列表中的状态
        const product = this.products.find(p => p.id === productId);
        if (product) {
          product.status = response.data.newStatus;
        }
        return response.data;
      } catch (error) {
        console.error('切换商品状态失败:', error);
        throw error;
      }
    }
  },
});
