package com.example.back_end.service;

import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {
    private final TransactionRepository transactionRepository;

    public StatisticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public StatisticsResponse monthly(Long bookId, String month) {
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        List<Object[]> rows = transactionRepository.aggregateByCategory(bookId, monthStart, monthEnd);

        List<StatisticsResponse.CategoryStatItem> expenseItems = new ArrayList<>();
        List<StatisticsResponse.CategoryStatItem> incomeItems = new ArrayList<>();
        long totalExpense = 0;
        long totalIncome = 0;

        for (Object[] row : rows) {
            Integer type = (Integer) row[0];
            Long categoryId = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Long total = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long count = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            StatisticsResponse.CategoryStatItem item = new StatisticsResponse.CategoryStatItem(categoryId, total, count);
            if (type == 0) {
                totalExpense += total;
                expenseItems.add(item);
            } else if (type == 1) {
                totalIncome += total;
                incomeItems.add(item);
            }
        }

        return new StatisticsResponse(totalExpense, totalIncome, expenseItems, incomeItems);
    }
}
