package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 账户实体（账本下的具体账户，如：现金、银行卡、信用卡等）
 */
@Data
@TableName("account")
public class Account {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属账本 ID（关联 account_book 表） */
    private Long bookId;

    /** 账户名称 */
    private String name;

    /** 账户类型：1-现金，2-储蓄卡，3-信用卡，4-支付宝，5-微信支付等 */
    private Integer type;

    /** 货币类型，默认 CNY（人民币） */
    private String currency;

    /** 账户图标 URL */
    private String icon;

    /** 初始余额（单位：分，避免浮点精度问题） */
    private Integer initialBalance;

    /** 是否计入总资产：0-不计入，1-计入（默认） */
    private Integer isIncludeTotal;

    /** 排序顺序，数值越小越靠前 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
