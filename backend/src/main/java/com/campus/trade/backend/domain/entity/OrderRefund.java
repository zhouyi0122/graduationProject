package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_refund")
public class OrderRefund {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Integer status; // 0:退款中, 1:退款成功, 2:退款失败

    private String reason;

    private String description;

    private Integer disputeInProgress; // 0:否, 1:是

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
