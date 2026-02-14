package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.ProductDTO;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.ProductMapper;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private com.campus.trade.backend.service.UserService userService;

    @Autowired
    private com.campus.trade.backend.mapper.ProductImageMapper productImageMapper;

    @Autowired
    private com.campus.trade.backend.mapper.FavoriteMapper favoriteMapper;

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
        // 显式设置 condition，防止 copyProperties 出现问题
        product.setCondition(productDTO.getCondition());
        
        // 确保 categoryId 哪怕前端没传也有默认值
        if (product.getCategoryId() == null) {
            product.setCategoryId(0);
        }

        // 插入商品数据到数据库
        this.save(product);

        return product;
    }

    @Override
    public Product getProductById(Long id) {
        Product product = this.getById(id);
        if (product != null) {
            // 增加浏览量
            product.setViews(product.getViews() + 1);
            this.updateById(product);

            // 关联查询卖家信息
            User seller = userService.getById(product.getSellerId());
            if (seller != null) {
                seller.setPassword(null);
                product.setSeller(seller);
            }

            // 关联查询商品图片
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.ProductImage> imgWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            imgWrapper.eq("product_id", product.getId()).orderByAsc("sort_order");
            List<com.campus.trade.backend.domain.entity.ProductImage> images = productImageMapper.selectList(imgWrapper);
            
            // 设置主图地址
            if (!images.isEmpty()) {
                product.setImageUrl(images.get(0).getImageUrl());
            } else {
                // 如果没有图，设置一个占位图
                product.setImageUrl("https://picsum.photos/400/300?random=" + product.getId());
            }

            // 统计真实收藏人数
            Long favCount = favoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.Favorite>()
                    .eq("product_id", id)
                    .eq("status", 1)
            );
            product.setFavoriteCount(favCount);

            // 判断当前用户是否已收藏
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                Long currentUserId = userDetails.getId();
                com.campus.trade.backend.domain.entity.Favorite favorite = favoriteMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.Favorite>()
                        .eq("product_id", id)
                        .eq("user_id", currentUserId)
                        .eq("status", 1)
                );
                product.setIsFavorited(favorite != null);
            }
        }
        return product;
    }

    @Override
    @Transactional
    public void updateProduct(Long id, ProductDTO productDTO) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在！");
        }

        // 鉴权：只能修改自己的商品
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails) || !product.getSellerId().equals(userDetails.getId())) {
            throw new RuntimeException("无权修改此商品！");
        }

        // 将DTO中的属性复制到实体中
        BeanUtils.copyProperties(productDTO, product);
        // 显式设置 condition
        product.setCondition(productDTO.getCondition());
        
        this.updateById(product);
    }

    @Override
    public List<Product> getAllProducts(String query, String sort) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<Product>()
                .eq("status", 0); // 只查询在售商品
        
        if (query != null && !query.trim().isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like("title", query)
                       .or()
                       .like("description", query)
            );
        }
        
        // 应用排序逻辑
        if (sort != null) {
            switch (sort) {
                case "latest":
                    queryWrapper.orderByDesc("create_time");
                    break;
                case "price_asc":
                    queryWrapper.orderByAsc("price");
                    break;
                case "price_desc":
                    queryWrapper.orderByDesc("price");
                    break;
                default:
                    queryWrapper.orderByDesc("create_time");
                    break;
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        
        List<Product> products = this.list(queryWrapper);
        
        for (Product product : products) {
            // 关联查询卖家信息
            User seller = userService.getById(product.getSellerId());
            if (seller != null) {
                // 为了安全，清空敏感信息
                seller.setPassword(null);
                product.setSeller(seller);
            }
            
            // 关联查询商品主图
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.ProductImage> imgWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            imgWrapper.eq("product_id", product.getId()).orderByDesc("is_primary").last("limit 1");
            com.campus.trade.backend.domain.entity.ProductImage mainImg = productImageMapper.selectOne(imgWrapper);
            if (mainImg != null) {
                product.setImageUrl(mainImg.getImageUrl());
            } else {
                // 如果没有图，设置一个占位图
                product.setImageUrl("https://picsum.photos/400/300?random=" + product.getId());
            }

            // 统计真实收藏人数
            Long favCount = favoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.Favorite>()
                    .eq("product_id", product.getId())
                    .eq("status", 1)
            );
            product.setFavoriteCount(favCount);
        }
        return products;
    }

    @Override
    public Integer toggleFavorite(Long productId, Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.Favorite> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("product_id", productId).eq("user_id", userId);
        
        com.campus.trade.backend.domain.entity.Favorite favorite = favoriteMapper.selectOne(queryWrapper);
        
        if (favorite == null) {
            // 新增收藏
            favorite = new com.campus.trade.backend.domain.entity.Favorite();
            favorite.setProductId(productId);
            favorite.setUserId(userId);
            favorite.setStatus(1); // 有效
            favoriteMapper.insert(favorite);
            return 1;
        } else {
            // 切换状态：1->2, 2->1
            Integer newStatus = favorite.getStatus() == 1 ? 2 : 1;
            favorite.setStatus(newStatus);
            favoriteMapper.updateById(favorite);
            return newStatus;
        }
    }
}
