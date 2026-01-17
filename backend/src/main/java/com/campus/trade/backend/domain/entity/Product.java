package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private Integer categoryId;

    private String title;

    private String description;

    private BigDecimal originalPrice;

    private BigDecimal price;

    private Integer status = 0; // 0:在售, 1:已售出, 2:下架

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
