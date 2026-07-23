package com.example.back_end.dto.request;

public record UpdateBudgetRequest(
        Long category_id,
        Integer amount,
        Integer period_type,
        String start_date,
        Integer carry_over,
        Integer status) {}
