package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("product_comment_reply")
public class ProductCommentReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    // 非数据库字段，用于返回回复用户信息
    @TableField(exist = false)
    private User user;
}
