package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.ProductDTO;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.mapper.ProductMapper;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Product createProduct(ProductDTO productDTO) {
        // 从安全上下文中获取当前登录的用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("用户未登录，无法发布商品！");
        }

        Long currentUserId = userDetails.getId();

        Product product = new Product();
        // 将DTO中的属性复制到实体中
        BeanUtils.copyProperties(productDTO, product);

        // 设置卖家ID为当前登录用户的ID
        product.setSellerId(currentUserId);
        // 设置商品初始状态为“在售”
        product.setStatus(0);

        // 插入商品数据到数据库
        this.save(product);

        return product;
    }
}












