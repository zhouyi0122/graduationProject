package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_refund_history")
public class OrderRefundHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long refundId;

    private String text; // 进度描述文案

    private Integer isRejection; // 是否为卖家拒绝记录 (0:否, 1:是)

    private String reason; // 拒绝/退款原因

    private String description; // 说明

    private Long rejectionTimestamp; // 卖家拒绝时间戳 (用于前端倒计时)

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
