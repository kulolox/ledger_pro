package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.dto.response.TrendResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.IStatisticsService;
import com.example.back_end.utils.AuthUtils;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements IStatisticsService {

    private final AccountBookMapper accountBookMapper;

    public StatisticsServiceImpl(AccountBookMapper accountBookMapper) {
        this.accountBookMapper = accountBookMapper;
    }

    public StatisticsResponse monthly(Long bookId, String month) {
        if (month == null || !month.matches("^\\d{4}-\\d{2}$")) {
            throw new BizException("月份格式不正确，应为 YYYY-MM");
        }
        validateBookOwnership(bookId);
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        List<Map<String, Object>> rows = baseMapper.sumByCategoryAndType(bookId, monthStart, monthEnd);

        List<StatisticsResponse.CategoryStatItem> expenseItems = new ArrayList<>();
        List<StatisticsResponse.CategoryStatItem> incomeItems = new ArrayList<>();
        long totalExpense = 0;
        long totalIncome = 0;

        for (Map<String, Object> row : rows) {
            Integer type = row.get("type") != null ? ((Number) row.get("type")).intValue() : 0;
            Long categoryId = row.get("category_id") != null ? ((Number) row.get("category_id")).longValue() : 0L;
            Long total = row.get("total") != null ? ((Number) row.get("total")).longValue() : 0L;
            Long count = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;

            StatisticsResponse.CategoryStatItem item =
                    new StatisticsResponse.CategoryStatItem(categoryId, total, count);
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

    public TrendResponse trend(Long bookId, int months) {
        validateBookOwnership(bookId);
        int safeMonths = months <= 0 ? 6 : Math.min(months, 36);

        YearMonth current = YearMonth.now();
        LocalDateTime start = current.minusMonths(safeMonths - 1L).atDay(1).atStartOfDay();
        LocalDateTime end = current.plusMonths(1).atDay(1).atStartOfDay();

        Map<String, Long> expenseByMonth = new HashMap<>();
        Map<String, Long> incomeByMonth = new HashMap<>();
        for (Map<String, Object> row : baseMapper.sumByMonthAndType(bookId, start, end)) {
            String ym = (String) row.get("ym");
            Integer type = row.get("type") != null ? ((Number) row.get("type")).intValue() : 0;
            Long total = row.get("total") != null ? ((Number) row.get("total")).longValue() : 0L;
            if (type != null && type == 0) expenseByMonth.put(ym, total);
            else if (type != null && type == 1) incomeByMonth.put(ym, total);
        }

        List<String> monthLabels = new ArrayList<>();
        List<Long> expenseList = new ArrayList<>();
        List<Long> incomeList = new ArrayList<>();
        for (int i = safeMonths - 1; i >= 0; i--) {
            String label = current.minusMonths(i).toString();
            monthLabels.add(label);
            expenseList.add(expenseByMonth.getOrDefault(label, 0L));
            incomeList.add(incomeByMonth.getOrDefault(label, 0L));
        }

        return new TrendResponse(monthLabels, expenseList, incomeList);
    }

    private void validateBookOwnership(Long bookId) {
        if (bookId == null) {
            throw new BizException("账本ID不能为空");
        }
        AccountBook book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
    }
}
