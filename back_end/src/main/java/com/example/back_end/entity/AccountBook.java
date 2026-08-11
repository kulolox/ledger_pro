package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 账本实体
 */
@Data
@TableName("account_book")
public class AccountBook {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 账本名称 */
    private String name;

    /** 封面图标 URL */
    private String coverIcon;

    /** 账本类型：0-个人账本，1-家庭账本，2-企业账本等 */
    private Integer type;

    /** 账本所有者（创建者）的用户 ID */
    private Long ownerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
