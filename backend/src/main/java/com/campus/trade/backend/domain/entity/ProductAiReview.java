package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("product_ai_review")
public class ProductAiReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Integer riskScore = 0; // 0-10

    private String riskTags = "";

    private String aiReason = "";

    private Integer status = 0; // 0待人工审核,1人工通过,2人工下架

    private String aiModel = "qwen3-vl-plus";

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
