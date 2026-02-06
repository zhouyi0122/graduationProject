package com.campus.trade.backend.domain.dto;

import com.campus.trade.backend.domain.entity.Order;
import com.campus.trade.backend.domain.entity.Product;
import com.campus.trade.backend.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private Order order;
    private Product product;
    private User buyer;
    private User seller;
}
