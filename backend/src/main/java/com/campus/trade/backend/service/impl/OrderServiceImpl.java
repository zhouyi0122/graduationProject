package com.campus.trade.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.entity.Order;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.mapper.OrderMapper;
import com.campus.trade.backend.mapper.ProductMapper;
import com.campus.trade.backend.security.services.UserDetailsImpl;
import com.campus.trade.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Order createOrder(CreateOrderRequest createOrderRequest) {
        // 1. Get current user (buyer)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("用户未登录，无法创建订单！");
        }
        Long buyerId = userDetails.getId();

        // 2. Get product info
        Product product = productMapper.selectById(createOrderRequest.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在！");
        }

        // 3. Create and populate the order object
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setTotalPrice(product.getPrice());
        order.setStatus(0); // 0: 待付款

        // 4. Save the order to the database
        this.save(order);

        return order;
    }

    @Override
    public List<Order> getOrders(String type) {
        // 1. Get current user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new RuntimeException("用户未登录，无法获取订单！");
        }
        Long currentUserId = userDetails.getId();

        // 2. Build query based on type
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if ("sold".equalsIgnoreCase(type)) {
            queryWrapper.eq("seller_id", currentUserId);
        } else if ("bought".equalsIgnoreCase(type)) {
            queryWrapper.eq("buyer_id", currentUserId);
        } else {
            throw new IllegalArgumentException("无效的订单类型: " + type);
        }

        // 3. Fetch and return orders
        return this.list(queryWrapper);
    }
}
