package com.example.back_end.dto.response;

import com.example.back_end.entity.Category;

public record CategoryResponse(Long id, String name, Integer type, String icon, String color, Long parent_id) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon(), c.getColor(), c.getParentId());
    }
}
