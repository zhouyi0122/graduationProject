import apiClient from './api';

class AuthService {
  login(user) {
    return apiClient.post('/auth/login', {
      username: user.username,
      password: user.password
    });
  }

  register(user) {
    return apiClient.post('/auth/register', {
      username: user.username,
      password: user.password
    });
  }
}

export default new AuthService();


