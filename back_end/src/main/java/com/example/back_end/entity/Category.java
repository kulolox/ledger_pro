package com.example.back_end.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统分类实体（内置分类，非用户自定义）
 * 区别于 user_category（用户自定义分类）
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父分类 ID，0 表示顶级分类 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 分类类型：0-支出，1-收入等 */
    private Integer type;

    /** 分类图标 URL */
    private String icon;

    /** 分类颜色标识 */
    private String color;

    /** 排序顺序，数值越小越靠前 */
    private Integer sortOrder;

    /** 是否系统内置：0-否，1-是（通常系统分类为只读） */
    private Integer isSystem;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
