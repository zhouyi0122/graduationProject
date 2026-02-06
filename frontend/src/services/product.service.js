// import apiClient from './api';

/**
 * 模拟的商品服务 (ProductService)
 * 在此模式下，所有方法都不会与后端通信，而是返回模拟数据。
 */
class ProductService {
  /**
   * 模拟创建一个新商品
   * @param {object} productData - 包含商品信息的对象
   * @returns Promise
   */
  createProduct(productData) {
    console.log('【模拟发布商品】:', productData);
    // 模拟网络延迟
    return new Promise(resolve => {
      setTimeout(() => {
        // 模拟成功响应
        const mockResponse = {
          data: {
            ...productData,
            id: Date.now(), // Assign a temporary ID
            sellerId: 99, // Mock seller ID
            status: 0,
            createTime: new Date().toISOString(),
            updateTime: new Date().toISOString(),
          }
        };
        resolve(mockResponse);
      }, 500);
    });
  }
}

export default new ProductService();
