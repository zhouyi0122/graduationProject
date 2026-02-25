package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("product_draft")
public class ProductDraft {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private BigDecimal price;

    @TableField("`condition`")
    private String condition;

    @TableField(exist = false)
    private Integer categoryId;

    @TableField("images_json")
    private String imageUrls; // 映射到数据库的 images_json 列

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
