package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders") // 使用复数形式以避免与SQL关键字冲突
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNumber;

    private Long buyerId;

    private Long sellerId;

    private Long productId;

    private Integer status; // 0:待付款, 1:待发货, 2:待收货, 3:待评价, 4:已完成, 5:退款中, 6:已取消

    private BigDecimal totalPrice;

    private String shippingAddress;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Date payTime;

    private Date shipTime;

    private Date completeTime;
}
