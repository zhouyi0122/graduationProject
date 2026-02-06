// import apiClient from './api';

/**
 * 模拟的认证服务 (AuthService)
 * 在此模式下，所有方法都不会与后端通信，而是返回模拟数据。
 */
class AuthService {
  /**
   * 模拟登录
   * @param {object} user - 包含用户名和密码的对象
   * @returns Promise - 返回一个模拟的JwtResponse
   */
  login(user) {
    console.log('【模拟登录】用户名:', user.username);
    // 模拟网络延迟
    return new Promise(resolve => {
      setTimeout(() => {
        // 模拟成功响应，返回一个假的Token和用户信息
        const mockResponse = {
          data: {
            token: 'fake-jwt-token-for-testing',
            type: 'Bearer',
            id: 99,
            username: user.username,
            email: `${user.username}@example.com`,
            roles: ['ROLE_USER'],
          }
        };
        resolve(mockResponse);
      }, 500);
    });
  }

  /**
   * 模拟注册
   * @param {object} user - 包含用户名和密码的对象
   * @returns Promise - 返回一个模拟的成功消息
   */
  register(user) {
    console.log('【模拟注册】用户名:', user.username);
    // 模拟网络延迟
    return new Promise(resolve => {
      setTimeout(() => {
        const mockResponse = {
          data: { message: '注册成功！' }
        };
        resolve(mockResponse);
      }, 500);
    });
  }
}

export default new AuthService();
