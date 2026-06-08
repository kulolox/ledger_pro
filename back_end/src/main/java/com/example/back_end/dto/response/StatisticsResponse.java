package com.example.back_end.dto.response;

import java.util.List;

public record StatisticsResponse(
    Long total_expense,
    Long total_income,
    List<CategoryStatItem> expense_by_category,
    List<CategoryStatItem> income_by_category
) {
    public record CategoryStatItem(Long category_id, Long amount, Long count) {}
}
