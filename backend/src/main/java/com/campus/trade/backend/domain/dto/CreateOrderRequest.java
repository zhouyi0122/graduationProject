package com.campus.trade.backend.domain.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long productId;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
}
