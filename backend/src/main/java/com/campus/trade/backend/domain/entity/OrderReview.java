package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_review")
public class OrderReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long reviewerId;
    
    private String targetType; // buyer / seller
    
    private Integer rating; // 1-5
    
    private String content;
    
    private Integer isAdditional; // 0: 否, 1: 是
    
    private Long parentId; // 关联的父评价ID (用于卖家回复)
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    // 非数据库字段，用于返回评价人信息
    @TableField(exist = false)
    private User reviewer;
}
