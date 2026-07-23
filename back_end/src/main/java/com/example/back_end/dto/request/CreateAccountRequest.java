package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull Long book_id,
        @NotBlank String name,
        @NotNull Integer type,
        String icon,
        String currency,
        Integer initial_balance,
        Integer is_include_total,
        Integer sort_order) {}
