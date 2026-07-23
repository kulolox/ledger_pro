package com.example.back_end.dto.response;

public record BudgetResponse(
        Long id,
        Long book_id,
        Long category_id,
        Integer amount,
        Integer period_type,
        String start_date,
        Integer carry_over,
        Integer status,
        Long spent,       // 当月已用金额(分)
        Integer percent)  // 进度百分比 0-100
{}
