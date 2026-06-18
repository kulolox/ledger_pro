package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @Size(max = 20) String name,
        Integer type,
        @Size(max = 50) String icon,
        @Size(max = 20) String color,
        Long parent_id,
        Integer sort_order,
        Integer visible) {}
