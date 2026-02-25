package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.dto.OrderDetailDTO;
import com.campus.trade.backend.domain.entity.Order;
import com.campus.trade.backend.service.OrderService;
import com.campus.trade.backend.service.UserService;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest, org.springframework.validation.BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }
        try {
        Order createdOrder = orderService.createOrder(createOrderRequest);
        return ResponseEntity.ok(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestParam String type) {
        List<Order> orders = orderService.getOrders(type);
        return ResponseEntity.ok(orders);
    }

    @Autowired
    private com.campus.trade.backend.mapper.OrderReviewMapper orderReviewMapper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        try {
            OrderDetailDTO detail = orderService.getOrderDetail(id);
            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 提交订单评价
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<?> postOrderReview(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(401).body("未登录");
        }

        Integer rating = (Integer) request.get("rating");
        String content = (String) request.get("comment"); 
        Integer isAdditional = (Integer) request.get("isAdditional");
        Object parentIdObj = request.get("parentId");
        Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;
        String targetType = (String) request.get("targetType");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("评价内容不能为空");
        }

        // 1. 保存评价/追评/回复
        com.campus.trade.backend.domain.entity.OrderReview review = new com.campus.trade.backend.domain.entity.OrderReview();
        review.setOrderId(id);
        review.setReviewerId(userDetails.getId());
        review.setRating(rating != null ? rating : 5);
        review.setContent(content);
        review.setTargetType(targetType != null ? targetType : "seller");
        review.setIsAdditional(isAdditional != null ? isAdditional : 0);
        review.setParentId(parentId);
        orderReviewMapper.insert(review);

        // 2. 如果是首次评价，更新订单状态为已完成 (4)
        if (review.getIsAdditional() == 0 && review.getParentId() == null) {
            orderService.updateOrderStatus(id, 4);
        }

        return ResponseEntity.ok(Map.of("message", "提交成功"));
    }

    /**
     * 获取订单评价
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getOrderReviews(@PathVariable Long id) {
        List<com.campus.trade.backend.domain.entity.OrderReview> reviews = orderReviewMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.trade.backend.domain.entity.OrderReview>()
                .eq("order_id", id)
                .orderByAsc("create_time")
        );
        
        for (com.campus.trade.backend.domain.entity.OrderReview review : reviews) {
            com.campus.trade.backend.domain.entity.User reviewer = userService.getById(review.getReviewerId());
            if (reviewer != null) {
                reviewer.setPassword(null);
                review.setReviewer(reviewer);
            }
        }
        return ResponseEntity.ok(reviews);
    }

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param request 包含新状态的请求体
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body("状态码不能为空");
            }
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<?> applyRefund(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body("未登录");
        }

        String reason = request.get("reason");
        String description = request.get("description");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("退款原因不能为空");
        }
        try {
            orderService.applyRefund(id, reason, description);
            return ResponseEntity.ok(Map.of("message", "退款申请已提交"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 处理退款申请 (卖家操作)
     */
    @PutMapping("/{id}/refund/handle")
    public ResponseEntity<?> handleRefund(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body("未登录");
        }

        String action = request.get("action"); // agree 或 reject
        String reason = request.get("reason");

        if (action == null) {
            return ResponseEntity.badRequest().body("处理动作不能为空");
        }

        try {
            orderService.handleRefund(id, action, reason);
            return ResponseEntity.ok(Map.of("message", "处理成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 申请维权
     */
    @PostMapping("/{id}/refund/dispute")
    public ResponseEntity<?> applyDispute(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body("未登录");
        }
        try {
            orderService.applyDispute(id);
            return ResponseEntity.ok(Map.of("message", "维权申请已提交，请等待管理员处理"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
