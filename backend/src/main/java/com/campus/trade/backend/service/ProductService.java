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
}

