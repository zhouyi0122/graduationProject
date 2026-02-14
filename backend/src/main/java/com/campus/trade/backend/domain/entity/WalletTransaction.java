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
@TableName("wallet_transaction")
public class WalletTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type; // 0:充值, 1:消费, 2:提现, 3:卖出收入
    private BigDecimal amount;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
