package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("conversation_message")
public class ConversationMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long conversationId;
    
    private Long senderId;
    
    private String content;
    
    private Integer isRead; // 0: 未读, 1: 已读
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
