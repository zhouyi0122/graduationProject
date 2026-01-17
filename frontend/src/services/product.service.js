import apiClient from './api';

/**
 * 商品服务
 * 负责与后端商品相关的API进行交互
 */
class ProductService {
  /**
   * 创建一个新商品
   * @param {object} productData - 包含商品信息的对象 (title, description, categoryId, price)
   * @returns Promise
   */
  createProduct(productData) {
    return apiClient.post('/products', productData);
  }

  // 后续可以添加获取商品列表、获取商品详情等方法
  // getProducts() { ... }
  // getProduct(id) { ... }
}

export default new ProductService();

