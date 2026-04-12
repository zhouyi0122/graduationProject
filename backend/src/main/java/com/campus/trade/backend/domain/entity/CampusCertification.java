package com.campus.trade.backend.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("campus_certification")
public class CampusCertification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String school;

    private String studentId;

    private String realName;

    private Integer status = 0; // 0:待审核, 1:通过, 2:驳回

    private String reviewRemark;

    private Long reviewerId;

    private Date reviewTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
