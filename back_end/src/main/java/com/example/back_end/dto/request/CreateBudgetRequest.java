package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateBudgetRequest(
        @NotNull Long book_id,
        Long category_id,
        @NotNull Integer amount,
        @NotNull Integer period_type,
        @NotNull String start_date,
        Integer carry_over) {}
