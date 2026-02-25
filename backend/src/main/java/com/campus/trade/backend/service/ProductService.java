package com.campus.trade.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.backend.domain.dto.ProductDTO;
import com.campus.trade.backend.domain.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建一个新商品
     * @param productDTO 包含商品信息的DTO
     * @return 创建成功后的商品实体
     */
    Product createProduct(ProductDTO productDTO);

    /**
     * 获取所有在售商品列表，支持关键字搜索和排序
     * @param query 搜索关键字
     * @param sort 排序方式 (latest, price_asc, price_desc, fav)
     * @return 商品列表
     */
    java.util.List<Product> getAllProducts(String query, String sort);

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    Product getProductById(Long id);

    /**
     * 更新商品信息
     * @param id 商品ID
     * @param productDTO 更新后的商品信息
     */
    void updateProduct(Long id, ProductDTO productDTO);

    /**
     * 切换收藏状态
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 切换后的收藏状态（1:已收藏, 2:未收藏）
     */
    Integer toggleFavorite(Long productId, Long userId);

    /**
     * 获取指定用户发布的商品列表（包含主图）
     * @param userId 用户ID
     * @return 商品列表
     */
    java.util.List<Product> getMyProducts(Long userId);
}












