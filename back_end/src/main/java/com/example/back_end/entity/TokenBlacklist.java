package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Token 黑名单实体
 * 用于存储已注销的 JWT 令牌，实现登出功能
 */
@Data
@TableName("token_blacklist")
public class TokenBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 令牌哈希值（使用 SHA-256 对 JWT 进行哈希，避免存储过长的原始令牌） */
    private String tokenHash;

    /** 令牌过期时间（与 JWT 的过期时间一致，用于自动清理） */
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记（可选）
     * 黑名单通常不需要逻辑删除，因为过期数据可直接物理删除。
     * 如果业务需要"恢复"已加入黑名单的令牌，可启用此字段。
     */
    // @TableLogic
    // private Integer deleted;
}
