package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 商品图片实体
 */
@Data
@TableName("product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String imageUrl;

    private Integer isPrimary = 0; // 0:否, 1:是

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}

