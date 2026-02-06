package com.campus.trade.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(CreateOrderRequest createOrderRequest);

    List<Order> getOrders(String type);

    OrderDetailDTO getOrderDetail(Long orderId);
}
