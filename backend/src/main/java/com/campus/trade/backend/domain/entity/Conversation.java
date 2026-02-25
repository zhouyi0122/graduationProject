package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long user1Id;
    
    private Long user2Id;
    
    private Integer isSupport; // 0: 普通会话, 1: 客服/维权会话
    
    private String lastMessage;
    
    private Date lastTime;

    // 非数据库字段，用于返回对方用户信息
    @TableField(exist = false)
    private User otherUser;

    @TableField(exist = false)
    private Integer unreadCount = 0;
}
