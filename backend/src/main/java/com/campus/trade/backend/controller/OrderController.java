package com.campus.trade.backend.controller;

import com.campus.trade.backend.domain.dto.CreateOrderRequest;
import com.campus.trade.backend.domain.entity.Order;
import com.campus.trade.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        Order createdOrder = orderService.createOrder(createOrderRequest);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getOrders(@RequestParam String type) {
        List<Order> orders = orderService.getOrders(type);
        return ResponseEntity.ok(orders);
    }
}
