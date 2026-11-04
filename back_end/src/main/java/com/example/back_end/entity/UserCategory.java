package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户分类实体
 */
@Data
@TableName("user_category")
public class UserCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户 ID（如果为 null，则可能是系统分类） */
    private Long userId;

    /** 来源模板 ID（null 表示用户完全自定义） */
    private Long sourceId;

    /** 分类名称 */
    private String name;

    /** 分类类型：0-普通，1-特殊类型等 */
    private Integer type;

    /** 分类图标 URL */
    private String icon;

    /** 分类颜色标识 */
    private String color;

    /** 父分类 ID（用于构建树形结构），0 表示顶级分类 */
    private Long parentId;

    /** 是否系统内置分类：0-否，1-是 */
    private Integer isSystem;

    /** 是否可见：0-隐藏，1-可见 */
    private Integer visible;

    /** 排序顺序，数值越小越靠前 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
