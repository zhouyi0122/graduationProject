package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.entity.Notification;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.domain.entity.User;
import com.campus.trade.backend.mapper.NotificationMapper;
import com.campus.trade.backend.service.OrderService;
import com.campus.trade.backend.service.ProductService;
import com.campus.trade.backend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.list(new QueryWrapper<Product>().orderByDesc("create_time"));
        // 关联卖家信息
        products.forEach(p -> {
            User seller = userService.getById(p.getSellerId());
            if (seller != null) {
                seller.setPassword(null);
                p.setSeller(seller);
            }
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
}
