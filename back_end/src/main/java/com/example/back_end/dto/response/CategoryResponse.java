package com.example.back_end.dto.response;

import com.example.back_end.entity.Category;
import com.example.back_end.entity.UserCategory;

public record CategoryResponse(
        Long id,
        String name,
        Integer type,
        String icon,
        String color,
        Long parent_id,
        Integer is_system,
        Integer sort_order,
        Integer visible) {
    public static CategoryResponse from(UserCategory uc) {
        return new CategoryResponse(
                uc.getId(),
                uc.getName(),
                uc.getType(),
                uc.getIcon(),
                uc.getColor(),
                uc.getParentId(),
                uc.getIsSystem(),
                uc.getSortOrder(),
                uc.getVisible());
    }

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getType(),
                c.getIcon(),
                c.getColor(),
                c.getParentId(),
                c.getIsSystem(),
                c.getSortOrder(),
                1);
    }
}
