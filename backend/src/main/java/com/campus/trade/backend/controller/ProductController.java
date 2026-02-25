package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.dto.ProductDTO;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.domain.entity.ProductDraft;
import com.campus.trade.backend.mapper.ProductDraftMapper;
import com.campus.trade.backend.service.ProductService;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private ProductDraftMapper productDraftMapper;

    @Autowired
    private com.campus.trade.backend.mapper.ProductCommentMapper productCommentMapper;

    @Autowired
    private com.campus.trade.backend.mapper.ProductCommentReplyMapper productCommentReplyMapper;

    @Autowired
    private com.campus.trade.backend.mapper.FavoriteMapper favoriteMapper;

    @Autowired
    private com.campus.trade.backend.service.UserService userService;

    /**
     * 发布新商品
     * 只有登录用户才能访问此接口
     * @param productDTO 包含商品信息的请求体
     * @return 创建成功的商品信息
     */
    @PostMapping(produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO, org.springframework.validation.BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }
        Product createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * 获取所有在售商品列表，支持关键字搜索和排序
     * @param q 搜索关键字 (可选)
     * @param sort 排序方式 (可选, latest, price_asc, price_desc, fav)
     * @return 商品列表
     */
    @GetMapping("")
    public ResponseEntity<java.util.List<Product>> getAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort) {
        try {
            return ResponseEntity.ok(productService.getAllProducts(q, sort));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    /**
     * 获取当前用户发布的商品列表
     */
    @GetMapping("/my")
    public ResponseEntity<List<Product>> getMyProducts() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = userDetails.getId();
        
        // 调用服务层获取包含图片信息的商品列表
        List<Product> products = productService.getMyProducts(userId);
        return ResponseEntity.ok(products);
    }

    /**
     * 用户操作商品状态（下架/重新上架）
     */
    @PutMapping("/{id}/my-status")
    public ResponseEntity<?> updateMyProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 鉴权：只能修改自己的商品
        if (!product.getSellerId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("无权操作此商品");
        }

        Integer status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body("状态不能为空");
        }
        
        product.setStatus(status);
        productService.updateById(product);
        return ResponseEntity.ok(Map.of("message", "更新成功", "newStatus", status));
    }

    /**
     * 用户删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMyProduct(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).build();
        }

        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        // 鉴权：只能删除自己的商品
        if (!product.getSellerId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("无权操作此商品");
        }

        productService.removeById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(Map.of("message", "商品更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取商品留言列表及其回复
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getProductComments(@PathVariable Long id) {
        List<com.campus.trade.backend.domain.entity.ProductComment> comments = productCommentMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.ProductComment>()
                .eq("product_id", id)
                .orderByDesc("create_time")
        );

        for (com.campus.trade.backend.domain.entity.ProductComment comment : comments) {
            // 关联留言用户信息
            com.campus.trade.backend.domain.entity.User user = userService.getById(comment.getUserId());
            if (user != null) {
                user.setPassword(null);
                comment.setUser(user);
            }

            // 关联回复列表
            List<com.campus.trade.backend.domain.entity.ProductCommentReply> replies = productCommentReplyMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.ProductCommentReply>()
                    .eq("comment_id", comment.getId())
                    .orderByAsc("create_time")
            );
            
            for (com.campus.trade.backend.domain.entity.ProductCommentReply reply : replies) {
                com.campus.trade.backend.domain.entity.User replyUser = userService.getById(reply.getUserId());
                if (replyUser != null) {
                    replyUser.setPassword(null);
                    reply.setUser(replyUser);
                }
            }
            comment.setReplies(replies);
        }
        return ResponseEntity.ok(comments);
    }

    /**
     * 发表商品留言
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> postComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("留言内容不能为空");
        }

        com.campus.trade.backend.domain.entity.ProductComment comment = new com.campus.trade.backend.domain.entity.ProductComment();
        comment.setProductId(id);
        comment.setUserId(userDetails.getId());
        comment.setContent(content);
        productCommentMapper.insert(comment);

        return ResponseEntity.ok(Map.of("message", "留言成功", "commentId", comment.getId()));
    }

    /**
     * 切换商品收藏状态
     * @param id 商品ID
     * @return 切换后的状态 (1:已收藏, 2:取消收藏)
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        Integer status = productService.toggleFavorite(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("status", status, "message", status == 1 ? "收藏成功" : "取消收藏成功"));
    }

    /**
     * 获取当前用户收藏商品列表
     */
    @GetMapping("/favorites/my")
    public ResponseEntity<?> getMyFavorites() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        Long userId = userDetails.getId();
        // 1. 获取用户所有有效的收藏记录 (status = 1)
        List<com.campus.trade.backend.domain.entity.Favorite> favorites = favoriteMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.Favorite>()
                .eq("user_id", userId)
                .eq("status", 1)
                .orderByDesc("create_time")
        );
        
        // 2. 获取对应的商品详情
        List<Product> products = favorites.stream().map(fav -> {
            Product p = productService.getProductById(fav.getProductId());
            if (p != null) {
                p.setIsFavorited(true); // 列表页回显状态
            }
            return p;
        }).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(products);
    }

    /**
     * 回复商品留言
     */
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> postReply(@PathVariable Long commentId, @RequestBody Map<String, String> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("回复内容不能为空");
        }

        com.campus.trade.backend.domain.entity.ProductCommentReply reply = new com.campus.trade.backend.domain.entity.ProductCommentReply();
        reply.setCommentId(commentId);
        reply.setUserId(userDetails.getId());
        reply.setContent(content);
        productCommentReplyMapper.insert(reply);

        return ResponseEntity.ok(Map.of("message", "回复成功", "replyId", reply.getId()));
    }

    // --- 草稿管理接口 ---

    /**
     * 获取当前用户的草稿列表
     */
    @GetMapping("/drafts")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getDrafts() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        List<ProductDraft> drafts = productDraftMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProductDraft>()
                .eq("user_id", userDetails.getId())
                .orderByDesc("create_time")
        );
        return ResponseEntity.ok(drafts);
    }

    /**
     * 保存或更新草稿
     */
    @PostMapping("/drafts")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> saveDraft(@RequestBody Map<String, Object> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        ProductDraft draft = new ProductDraft();
        if (request.get("id") != null) {
            draft.setId(Long.valueOf(request.get("id").toString()));
        }
        draft.setTitle((String) request.get("title"));
        draft.setDescription((String) request.get("description"));
        if (request.get("price") != null) {
            draft.setPrice(new java.math.BigDecimal(request.get("price").toString()));
        }
        draft.setCondition((String) request.get("condition"));
        if (request.get("categoryId") != null) {
            draft.setCategoryId(Integer.valueOf(request.get("categoryId").toString()));
        }

        // 优先处理 images 数组（前端传来的新格式），其次处理 imageUrls 字符串
        Object imagesObj = request.get("images");
        String finalImageUrls = "";
        if (imagesObj instanceof java.util.List) {
            java.util.List<String> images = (java.util.List<String>) imagesObj;
            finalImageUrls = images.stream()
                .filter(url -> url != null && !url.startsWith("blob:"))
                .collect(java.util.stream.Collectors.joining(","));
        } else if (request.get("imageUrls") != null) {
            String imageUrlsStr = (String) request.get("imageUrls");
            finalImageUrls = java.util.Arrays.stream(imageUrlsStr.split(","))
                .filter(url -> url != null && !url.trim().isEmpty() && !url.startsWith("blob:"))
                .collect(java.util.stream.Collectors.joining(","));
        }
        draft.setImageUrls(finalImageUrls);
        
        draft.setUserId(userDetails.getId());
        if (draft.getId() == null) {
            productDraftMapper.insert(draft);
        } else {
            productDraftMapper.updateById(draft);
        }
        return ResponseEntity.ok(draft);
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/drafts/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDraft(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }
        
        ProductDraft draft = productDraftMapper.selectById(id);
        if (draft != null && draft.getUserId().equals(userDetails.getId())) {
            productDraftMapper.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "草稿删除成功"));
        }
        return ResponseEntity.status(403).body("无权删除此草稿");
    }
}
