package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.entity.CampusCertification;
import com.campus.trade.backend.domain.entity.Notification;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.domain.entity.ProductAiReview;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.CampusCertificationMapper;
import com.campus.trade.backend.mapper.NotificationMapper;
import com.campus.trade.backend.mapper.ProductAiReviewMapper;
import com.campus.trade.backend.mapper.ProductImageMapper;
import com.campus.trade.backend.service.AiProductAssistantService;
import com.campus.trade.backend.service.OrderService;
import com.campus.trade.backend.service.ProductService;
import com.campus.trade.backend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private CampusCertificationMapper campusCertificationMapper;

    @Autowired
    private ProductAiReviewMapper productAiReviewMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private AiProductAssistantService aiProductAssistantService;

    @Autowired
    private com.campus.trade.backend.mapper.OrderRefundMapper orderRefundMapper;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 总用户数
        stats.put("totalUsers", userService.count());
        
        // 2. 总商品数 (在售状态为0)
        stats.put("totalProducts", productService.count(new QueryWrapper<Product>().eq("status", 0)));
        
        // 3. 总订单数
        stats.put("totalOrders", orderService.count());
        
        // 4. 今日新增订单数
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        stats.put("todayOrders", orderService.count(new QueryWrapper<com.campus.trade.backend.domain.entity.Order>().ge("create_time", startOfDay)));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.list();
        // 敏感信息脱敏
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * 切换用户状态 (0: 正常, 1: 禁用)
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 切换状态
        user.setStatus(user.getStatus() == 0 ? 1 : 0);
        userService.updateById(user);
        
        return ResponseEntity.ok(Map.of(
            "message", "用户状态更新成功",
            "newStatus", user.getStatus()
        ));
    }

    /**
     * 获取所有商品列表（包含已下架的）
     * 排序规则：
     * 1) 在售且待审核(最优先)
     * 2) 其他在售
     * 3) 其他状态
     * 同组内按创建时间倒序
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(@RequestParam(required = false) Integer status) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<Product>().orderByDesc("create_time");
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        List<Product> products = productService.list(queryWrapper);

        products.forEach(p -> {
            User seller = userService.getById(p.getSellerId());
            if (seller != null) {
                seller.setPassword(null);
                p.setSeller(seller);
            }

            ProductAiReview review = productAiReviewMapper.selectOne(
                    new QueryWrapper<ProductAiReview>().eq("product_id", p.getId())
            );
            p.setAiReviewStatus(review == null ? null : review.getStatus());
        });

        products.sort((a, b) -> {
            int wa = productSortWeight(a);
            int wb = productSortWeight(b);
            if (wa != wb) return Integer.compare(wa, wb);
            Date ta = a.getCreateTime();
            Date tb = b.getCreateTime();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        return ResponseEntity.ok(products);
    }

    /**
     * 管理员强制下架/重新上架商品
     */
    @PutMapping("/products/{id}/status")
    public ResponseEntity<?> toggleProductStatus(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 状态切换逻辑：0(在售) <-> 2(管理员下架)
        // 注意：1(已售出) 的商品通常不建议由管理员改为在售
        int currentStatus = product.getStatus();
        int newStatus = (currentStatus == 0) ? 2 : 0;
        
        product.setStatus(newStatus);
        productService.updateById(product);
        
        return ResponseEntity.ok(Map.of(
            "message", "商品状态更新成功",
            "newStatus", newStatus
        ));
    }

    /**
     * 获取全平台订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<List<com.campus.trade.backend.domain.entity.Order>> getAllOrders() {
        List<com.campus.trade.backend.domain.entity.Order> orders = orderService.list(
            new QueryWrapper<com.campus.trade.backend.domain.entity.Order>()
                .orderByDesc("create_time")
        );
        
        for (com.campus.trade.backend.domain.entity.Order order : orders) {
            // 关联商品标题
            Product product = productService.getById(order.getProductId());
            if (product != null) {
                order.setProductTitle(product.getTitle());
            }
            // 关联买家昵称
            User buyer = userService.getById(order.getBuyerId());
            if (buyer != null) {
                order.setBuyerNickname(buyer.getNickname());
            }
            // 关联卖家昵称
            User seller = userService.getById(order.getSellerId());
            if (seller != null) {
                order.setSellerNickname(seller.getNickname());
            }
            // 检查是否有正在进行的维权
            com.campus.trade.backend.domain.entity.OrderRefund refund = orderRefundMapper.selectOne(
                new QueryWrapper<com.campus.trade.backend.domain.entity.OrderRefund>().eq("order_id", order.getId())
            );
            
            if (refund != null) {
                order.setDisputeInProgress(refund.getDisputeInProgress());
            } else {
                order.setDisputeInProgress(0);
            }
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * 发送系统通知
     */
    @PostMapping("/notifications")
    public ResponseEntity<?> sendNotification(@RequestBody Notification notification) {
        if (notification.getTitle() == null || notification.getContent() == null) {
            return ResponseEntity.badRequest().body("标题和内容不能为空");
        }
        notificationMapper.insert(notification);
        return ResponseEntity.ok(Map.of("message", "通知发送成功"));
    }

    /**
     * 获取历史通知列表
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationMapper.selectList(
            new QueryWrapper<Notification>().orderByDesc("create_time")
        ));
    }

    /**
     * 管理员处理维权 (强制退款/驳回维权)
     */
    @PutMapping("/orders/{id}/refund/handle")
    public ResponseEntity<?> handleRefundDispute(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String action = request.get("action"); // force_refund 或 close_refund
        if (action == null) {
            return ResponseEntity.badRequest().body("处理动作不能为空");
        }
        try {
            orderService.adminHandleRefund(id, action);
            return ResponseEntity.ok(Map.of("message", "维权处理成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取校园认证申请列表
     */
    @GetMapping("/certifications")
    public ResponseEntity<?> getCampusCertifications(@RequestParam(required = false) Integer status) {
        QueryWrapper<CampusCertification> queryWrapper = new QueryWrapper<CampusCertification>().orderByDesc("create_time");
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        List<CampusCertification> certifications = campusCertificationMapper.selectList(queryWrapper);
        List<Map<String, Object>> result = certifications.stream().map(cert -> {
            User user = userService.getById(cert.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", cert.getId());
            item.put("userId", cert.getUserId());
            item.put("username", user != null ? user.getUsername() : "");
            item.put("nickname", user != null ? user.getNickname() : "");
            item.put("school", cert.getSchool());
            item.put("studentId", cert.getStudentId());
            item.put("realName", cert.getRealName());
            item.put("status", cert.getStatus());
            item.put("reviewRemark", cert.getReviewRemark());
            item.put("reviewerId", cert.getReviewerId());
            item.put("reviewTime", cert.getReviewTime());
            item.put("createTime", cert.getCreateTime());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * 审核通过校园认证
     */
    @PutMapping("/certifications/{id}/approve")
    public ResponseEntity<?> approveCampusCertification(@PathVariable Long id) {
        CampusCertification certification = campusCertificationMapper.selectById(id);
        if (certification == null) {
            return ResponseEntity.notFound().build();
        }
        if (certification.getStatus() != 0) {
            return ResponseEntity.badRequest().body("该申请已处理");
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long reviewerId = null;
        if (principal instanceof com.campus.trade.backend.security.services.UserDetailsImpl userDetails) {
            reviewerId = userDetails.getId();
        }

        certification.setStatus(1);
        certification.setReviewerId(reviewerId);
        certification.setReviewTime(new Date());
        certification.setReviewRemark("审核通过");
        campusCertificationMapper.updateById(certification);

        User user = userService.getById(certification.getUserId());
        if (user != null) {
            user.setCertificationStatus(2);
            userService.updateById(user);
        }

        return ResponseEntity.ok(Map.of("message", "审核通过成功"));
    }

    /**
     * 驳回校园认证
     */
    @PutMapping("/certifications/{id}/reject")
    public ResponseEntity<?> rejectCampusCertification(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        CampusCertification certification = campusCertificationMapper.selectById(id);
        if (certification == null) {
            return ResponseEntity.notFound().build();
        }
        if (certification.getStatus() != 0) {
            return ResponseEntity.badRequest().body("该申请已处理");
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long reviewerId = null;
        if (principal instanceof com.campus.trade.backend.security.services.UserDetailsImpl userDetails) {
            reviewerId = userDetails.getId();
        }

        String remark = request != null ? request.get("remark") : null;

        certification.setStatus(2);
        certification.setReviewerId(reviewerId);
        certification.setReviewTime(new Date());
        certification.setReviewRemark((remark == null || remark.trim().isEmpty()) ? "审核驳回" : remark.trim());
        campusCertificationMapper.updateById(certification);

        User user = userService.getById(certification.getUserId());
        if (user != null) {
            user.setCertificationStatus(3);
            userService.updateById(user);
        }

        return ResponseEntity.ok(Map.of("message", "已驳回该申请"));
    }

    /**
     * 管理员查看AI审核列表
     */
    @GetMapping("/products/ai-reviews")
    public ResponseEntity<?> getProductAiReviews(@RequestParam(required = false) Integer status) {
        QueryWrapper<ProductAiReview> queryWrapper = new QueryWrapper<ProductAiReview>()
                .orderByDesc("risk_score")
                .orderByDesc("update_time");
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        List<ProductAiReview> reviews = productAiReviewMapper.selectList(queryWrapper);

        List<Map<String, Object>> result = reviews.stream().map(review -> {
            Product product = productService.getById(review.getProductId());
            User seller = null;
            if (product != null) {
                seller = userService.getById(product.getSellerId());
            }
            Map<String, Object> item = new HashMap<>();
            item.put("reviewId", review.getId());
            item.put("productId", review.getProductId());
            item.put("riskScore", review.getRiskScore());
            item.put("riskTags", review.getRiskTags());
            item.put("aiReason", review.getAiReason());
            item.put("status", review.getStatus());
            item.put("aiModel", review.getAiModel());
            item.put("updateTime", review.getUpdateTime());
            item.put("productTitle", product != null ? product.getTitle() : "");
            item.put("productPrice", product != null ? product.getPrice() : null);
            item.put("productStatus", product != null ? product.getStatus() : null);
            item.put("sellerNickname", seller != null ? seller.getNickname() : "");
            item.put("sellerUsername", seller != null ? seller.getUsername() : "");
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * 触发单个商品AI审核
     * 仅允许审核：在售且当前为待审核(0)或无记录商品
     */
    @PostMapping("/products/{id}/ai-review")
    public ResponseEntity<?> runProductAiReview(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        if (product.getStatus() == null || product.getStatus() != 0) {
            return ResponseEntity.badRequest().body("仅在售商品可进行AI审核");
        }

        ProductAiReview existing = productAiReviewMapper.selectOne(new QueryWrapper<ProductAiReview>().eq("product_id", id));
        if (existing != null && existing.getStatus() != null && existing.getStatus() == 1) {
            return ResponseEntity.badRequest().body("该商品已人工审核通过，无需重复审核");
        }

        List<String> imageUrls = productImageMapper.selectList(
                        new QueryWrapper<com.campus.trade.backend.domain.entity.ProductImage>().eq("product_id", id)
                )
                .stream()
                .map(com.campus.trade.backend.domain.entity.ProductImage::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());

        Map<String, Object> aiResult = aiProductAssistantService.reviewProductCompliance(
                product.getTitle(),
                product.getDescription(),
                product.getCondition(),
                product.getPrice(),
                imageUrls
        );

        int riskScore = 0;
        try {
            riskScore = Integer.parseInt(String.valueOf(aiResult.getOrDefault("riskScore", 0)));
        } catch (Exception ignored) {
        }
        if (riskScore < 0) riskScore = 0;
        if (riskScore > 10) riskScore = 10;

        String riskTags = String.valueOf(aiResult.getOrDefault("riskTags", ""));
        String reason = String.valueOf(aiResult.getOrDefault("aiReason", "未发现明显违规风险"));

        ProductAiReview review = existing;
        if (review == null) {
            review = new ProductAiReview();
            review.setProductId(id);
        }
        review.setRiskScore(riskScore);
        review.setRiskTags(riskTags);
        review.setAiReason(reason);
        review.setStatus(0);
        review.setAiModel("qwen3-vl-plus");

        if (review.getId() == null) {
            productAiReviewMapper.insert(review);
        } else {
            productAiReviewMapper.updateById(review);
        }

        return ResponseEntity.ok(Map.of(
                "message", "AI审核完成",
                "riskScore", riskScore,
                "riskTags", riskTags,
                "aiReason", reason
        ));
    }

    /**
     * 人工通过AI审核结果
     */
    @PutMapping("/products/ai-reviews/{reviewId}/approve")
    public ResponseEntity<?> approveProductAiReview(@PathVariable Long reviewId) {
        ProductAiReview review = productAiReviewMapper.selectById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        review.setStatus(1);
        productAiReviewMapper.updateById(review);
        return ResponseEntity.ok(Map.of("message", "已标记为人工通过"));
    }

    /**
     * 人工下架AI审核命中商品
     */
    @PutMapping("/products/ai-reviews/{reviewId}/off-shelf")
    public ResponseEntity<?> offShelfProductByAiReview(@PathVariable Long reviewId) {
        ProductAiReview review = productAiReviewMapper.selectById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        Product product = productService.getById(review.getProductId());
        if (product != null) {
            product.setStatus(2);
            productService.updateById(product);
        }

        review.setStatus(2);
        productAiReviewMapper.updateById(review);

        return ResponseEntity.ok(Map.of("message", "商品已下架并标记为人工下架"));
    }

    private int productSortWeight(Product p) {
        Integer ps = p.getStatus();
        Integer as = p.getAiReviewStatus();

        if (ps != null && ps == 0 && (as == null || as == 0)) {
            return 0; // 在售 + 待审核
        }
        if (ps != null && ps == 0) {
            return 1; // 在售 + 非待审核
        }
        return 2; // 其他状态
    }
}
