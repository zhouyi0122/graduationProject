package com.campus.trade.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.dto.OrderDetailDTO; 
import com.campus.trade.backend.domain.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(CreateOrderRequest createOrderRequest);

    List<Order> getOrders(String type);

    OrderDetailDTO getOrderDetail(Long orderId);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 目标状态
     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 申请退款
     * @param orderId 订单ID
     * @param reason 退款原因
     * @param description 退款说明
     */
    void applyRefund(Long orderId, String reason, String description);

    /**
     * 处理退款申请
     * @param orderId 订单ID
     * @param action 处理动作 (agree: 同意, reject: 拒绝)
     * @param reason 拒绝原因 (可选)
     */
    void handleRefund(Long orderId, String action, String reason);

    /**
     * 管理员强制处理退款（维权介入）
     * @param orderId 订单ID
     * @param action 处理动作 (force_refund: 强制退款, close_refund: 驳回维权)
     */
    void adminHandleRefund(Long orderId, String action);

    /**
     * 申请维权
     * @param orderId 订单ID
     */
    void applyDispute(Long orderId);
}
