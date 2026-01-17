import { defineStore } from 'pinia';
import AuthService from '../services/auth.service';

const user = JSON.parse(localStorage.getItem('user'));

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // initial state: check if user is already in localStorage
    user: user ? user : null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.user?.token,
  },
  actions: {
    async login(user) {
      try {
        const response = await AuthService.login(user);
        const userData = response.data;
        this.user = userData;
        localStorage.setItem('user', JSON.stringify(userData));
        return Promise.resolve(userData);
      } catch (error) {
        this.user = null;
        localStorage.removeItem('user');
        return Promise.reject(error);
      }
    },
    logout() {
      this.user = null;
      localStorage.removeItem('user');
    },
    async register(user) {
        return AuthService.register(user);
    }
  },
});

