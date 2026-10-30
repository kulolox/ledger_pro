package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 交易流水实体
 */
@Data
@TableName("transaction")
public class Transaction {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属账本 ID */
    private Long bookId;

    /** 交易类型：1-收入，2-支出，3-转账等 */
    private Integer type;

    /** 交易金额（单位：分，避免浮点精度问题） */
    private Integer amount;

    /** 主账户 ID（转出的账户） */
    private Long accountId;

    /** 转入账户 ID（仅转账类型时使用） */
    private Long toAccountId;

    /** 分类 ID（关联 user_category） */
    private Long categoryId;

    /** 交易实际发生时间（由业务传入，非自动填充） */
    private LocalDateTime transTime;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
