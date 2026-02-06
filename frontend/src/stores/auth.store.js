import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')),
    returnUrl: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.user,
    isAdmin: (state) => state.user && state.user.roles.includes('ROLE_ADMIN'),
  },
  actions: {
    async login(username, password) {
      // Simulate API call
      console.log(`【模拟登录】用户名: ${username}, 密码: ${password}`);
      return new Promise(resolve => {
        setTimeout(() => {
          const isAdmin = username === 'admin';
          const mockUser = {
            id: isAdmin ? 999 : 1,
            username: username,
            email: `${username}@example.com`,
            roles: isAdmin ? ['ROLE_ADMIN', 'ROLE_USER'] : ['ROLE_USER'],
            token: 'fake-jwt-token',
          };
          this.user = mockUser;
          localStorage.setItem('user', JSON.stringify(mockUser));
          resolve(isAdmin); // Resolve with the admin status
        }, 500);
      });
    },
    async register(user) {
        // Simulate API call
        console.log('【模拟注册】:', user);
        return new Promise(resolve => {
            setTimeout(() => {
                resolve({ message: '用户注册成功！' });
            }, 500);
        });
    },
    logout() {
      this.user = null;
      localStorage.removeItem('user');
    }
  }
});
