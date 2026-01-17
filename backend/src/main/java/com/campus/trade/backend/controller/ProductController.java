package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.dto.ProductDTO;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 * 负责处理商品相关的HTTP请求
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 发布新商品
     * 只有登录用户才能访问此接口
     * @param productDTO 包含商品信息的请求体
     * @return 创建成功的商品信息
     */
    @PostMapping(produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(createdProduct);
    }
}

