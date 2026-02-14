package com.campus.trade.backend.domain.dto;

import com.campus.trade.backend.domain.entity.OrderRefund;
import com.campus.trade.backend.domain.entity.OrderRefundHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundDTO {
    private OrderRefund refund;
    private List<OrderRefundHistory> history;
}
