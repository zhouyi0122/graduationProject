import { defineStore } from 'pinia';
import apiClient from '../services/api';

const availableSupportAgents = [
    { id: 'support-a', name: '官方客服-小A', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
    { id: 'support-b', name: '官方客服-小B', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
    { id: 'support-c', name: '官方客服-小C', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
];

export const useUserStore = defineStore('user', {
  state: () => ({
    profile: JSON.parse(localStorage.getItem('user')) || {
      username: '',
      nickname: '',
      email: '',
      phone: '',
      isCertified: false,
      bio: '',
      gender: '保密',
      avatarUrl: '',
      balance: 0,
      stats: {
        likes: 0,
        following: 0,
        followers: 0,
      },
      shippingInfo: {
        name: '',
        phone: '',
        address: '',
      },
    },
    myProducts: [],
    drafts: [],
    favoriteProducts: [],
    conversations: [],
    supportConversations: [],
    notifications: [],
    unreadNotificationsCount: 0,
    soldOrders: [],
    boughtOrders: [],
    allUsers: [],
    allProducts: [],
    transactions: [],
    allOrders: [],
  }),
  getters: {
    allConversations: (state) => [...state.conversations, ...state.supportConversations],
  },
  actions: {
    updateProfile(newProfileData) {
        this.profile = { ...this.profile, ...newProfileData };
    },
    async fetchTransactions() {
      try {
        const response = await apiClient.get('/users/transactions');
        this.transactions = response.data;
        return response.data;
      } catch (error) {
        console.error('获取交易记录失败:', error);
        throw error;
      }
    },
    async fetchProfile() {
      try {
        const response = await apiClient.get('/users/profile');
        const updatedProfile = response.data;
        this.profile = { ...this.profile, ...updatedProfile };
        const user = JSON.parse(localStorage.getItem('user'));
        if (user) {
          const newUser = { ...user, ...updatedProfile };
          localStorage.setItem('user', JSON.stringify(newUser));
        }
        return updatedProfile;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        throw error;
      }
    },
    async fetchOrders(type) {
      try {
        const response = await apiClient.get(`/orders?type=${type}`);
        if (type === 'sold') {
          this.soldOrders = response.data;
        } else {
          this.boughtOrders = response.data;
        }
        return response.data;
      } catch (error) {
        console.error(`获取${type === 'sold' ? '卖出' : '买到'}订单失败:`, error);
        throw error;
      }
    },
    async fetchAllUsers() {
      try {
        const response = await apiClient.get('/admin/users');
        this.allUsers = response.data;
        return response.data;
      } catch (error) {
        console.error('获取所有用户失败:', error);
        throw error;
      }
    },
    async adminToggleUserStatus(userId) {
      try {
        const response = await apiClient.put(`/admin/users/${userId}/status`);
        const user = this.allUsers.find(u => u.id === userId);
        if (user) {
          user.status = response.data.newStatus;
        }
        return response.data;
      } catch (error) {
        console.error('切换用户状态失败:', error);
        throw error;
      }
    },
    async fetchAllAdminOrders() {
      try {
        const response = await apiClient.get('/admin/orders');
        this.allOrders = response.data;
        return response.data;
      } catch (error) {
        console.error('获取全平台订单失败:', error);
        throw error;
      }
    },
    async recharge(amount) {
      try {
        const response = await apiClient.post('/users/recharge', { amount: Number(amount) });
        const newBalance = response.data.newBalance;
        this.profile.balance = newBalance;
        const user = JSON.parse(localStorage.getItem('user'));
        if (user) {
          user.balance = newBalance;
          localStorage.setItem('user', JSON.stringify(user));
        }
        return newBalance;
      } catch (error) {
        console.error('充值失败:', error);
        throw error;
      }
    },
    // --- 私信模块 Actions ---
    async fetchConversations() {
      try {
        const response = await apiClient.get('/chat/conversations');
        // 映射字段，确保 unreadCount 是数字类型
        this.conversations = response.data.map(conv => ({
          ...conv,
          unreadCount: Number(conv.unreadCount) || 0
        }));
        return this.conversations;
      } catch (error) {
        console.error('获取会话列表失败:', error);
        throw error;
      }
    },
    async getOrCreateConversation(otherUserId) {
      try {
        const response = await apiClient.post('/chat/conversations', { otherUserId });
        return response.data;
      } catch (error) {
        console.error('获取或创建会话失败:', error);
        throw error;
      }
    },
    async fetchMessages(conversationId) {
      try {
        const response = await apiClient.get(`/chat/conversations/${conversationId}/messages`);
        return response.data;
      } catch (error) {
        console.error('获取消息失败:', error);
        throw error;
      }
    },
    async sendChatMessage(conversationId, content) {
      try {
        const response = await apiClient.post('/chat/messages', { conversationId, content });
        return response.data;
      } catch (error) {
        console.error('发送消息失败:', error);
        throw error;
      }
    },
    // --- 管理员：通知管理 Actions ---
    async fetchAdminNotifications() {
      try {
        const response = await apiClient.get('/admin/notifications');
        this.notifications = response.data;
        return response.data;
      } catch (error) {
        console.error('获取通知列表失败:', error);
        throw error;
      }
    },
    async sendAdminNotification(notificationData) {
      try {
        const response = await apiClient.post('/admin/notifications', notificationData);
        await this.fetchAdminNotifications(); // 发送成功后刷新列表
        return response.data;
      } catch (error) {
        console.error('发送通知失败:', error);
        throw error;
      }
    },
    // 用户：获取系统通知
    async fetchNotifications() {
      try {
        const response = await apiClient.get('/users/notifications');
        this.notifications = response.data.list;
        this.unreadNotificationsCount = response.data.unreadCount;
        return response.data;
      } catch (error) {
        console.error('获取通知失败:', error);
        throw error;
      }
    },
    async markNotificationsAsRead() {
      try {
        await apiClient.put('/users/notifications/read');
        this.unreadNotificationsCount = 0;
      } catch (error) {
        console.error('标记通知已读失败:', error);
      }
    },
    // --- 我的发布管理 Actions ---
    async fetchMyProducts() {
      try {
        const response = await apiClient.get('/products/my');
        this.myProducts = response.data;
        return response.data;
      } catch (error) {
        console.error('获取我的发布失败:', error);
        throw error;
      }
    },
    async updateMyProductStatus(productId, status) {
      try {
        const response = await apiClient.put(`/products/${productId}/my-status`, { status });
        // 同步更新本地列表
        const product = this.myProducts.find(p => p.id === productId);
        if (product) product.status = status;
        return response.data;
      } catch (error) {
        console.error('更新商品状态失败:', error);
        throw error;
      }
    },
    async deleteMyProduct(productId) {
      try {
        await apiClient.delete(`/products/${productId}`);
        // 从本地列表中移除
        this.myProducts = this.myProducts.filter(p => p.id !== productId);
      } catch (error) {
        console.error('删除商品失败:', error);
        throw error;
      }
    },
    // --- 我的收藏管理 Actions ---
    async fetchMyFavorites() {
      try {
        const response = await apiClient.get('/products/favorites/my');
        this.favoriteProducts = response.data;
        return response.data;
      } catch (error) {
        console.error('获取我的收藏失败:', error);
        throw error;
      }
    },
    async updateUserProfile(profileData) {
      try {
        await apiClient.put('/users/profile', profileData);
        // 更新成功后，刷新 profile 状态和本地存储
        await this.fetchProfile();
      } catch (error) {
        console.error('更新个人资料失败:', error);
        throw error;
      }
    },
    // --- 草稿管理 Actions ---
    async fetchDrafts() {
      try {
        const response = await apiClient.get('/products/drafts');
        this.drafts = response.data;
        return response.data;
      } catch (error) {
        console.error('获取草稿列表失败:', error);
        throw error;
      }
    },
    async saveProductDraft(draftData) {
      try {
        // 过滤掉 blob URL，只保留正式的服务器 URL
        const filteredImages = Array.isArray(draftData.images) 
          ? draftData.images.filter(url => url && !url.startsWith('blob:'))
          : [];

        const payload = {
          id: draftData.id || null,
          title: draftData.title || '',
          description: draftData.description || '',
          price: draftData.price || 0,
          condition: draftData.condition || '',
          categoryId: draftData.categoryId || 1,
          imageUrls: filteredImages.join(',')
        };
        
        const response = await apiClient.post('/products/drafts', payload);
        await this.fetchDrafts(); // 刷新列表
        return response.data;
      } catch (error) {
        console.error('保存草稿失败:', error);
        throw error;
      }
    },
    async deleteProductDraft(draftId) {
      try {
        await apiClient.delete(`/products/drafts/${draftId}`);
        this.drafts = this.drafts.filter(d => d.id !== draftId);
      } catch (error) {
        console.error('删除草稿失败:', error);
        throw error;
      }
    },
    async certifyCampus(certificationData) {
      try {
        await apiClient.post('/users/certify', certificationData);
        await this.fetchProfile(); // 刷新认证状态
      } catch (error) {
        console.error('校园认证失败:', error);
        throw error;
      }
    },
    async changePassword(oldPassword, newPassword) {
      try {
        await apiClient.post('/users/change-password', { oldPassword, newPassword });
      } catch (error) {
        console.error('修改密码失败:', error);
        throw error;
      }
    },
    async markConversationAsRead(conversationId) {
      try {
        await apiClient.put(`/chat/conversations/${conversationId}/read`);
        const conversation = this.conversations.find(c => String(c.id) === String(conversationId));
        if (conversation) {
          conversation.unreadCount = 0;
        }
      } catch (error) {
        console.error('标记已读失败:', error);
      }
    }
  },
});
