package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("product_comment")
public class ProductComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long userId;
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    // 非数据库字段，用于返回用户信息和回复列表
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private List<ProductCommentReply> replies;
}
