package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.dto.response.TrendResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.IStatisticsService;
import com.example.back_end.utils.AuthUtils;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
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
        validateBookOwnership(bookId);
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
        wrapper.select("type", "category_id", "SUM(amount) as total", "COUNT(*) as count")
                .eq("book_id", bookId)
                .ge("trans_time", monthStart)
                .lt("trans_time", monthEnd)
                .groupBy("type", "category_id")
                .orderByAsc("type")
                .orderByDesc("SUM(amount)");

        List<Map<String, Object>> rows = listMaps(wrapper);

        List<StatisticsResponse.CategoryStatItem> expenseItems = new ArrayList<>();
        List<StatisticsResponse.CategoryStatItem> incomeItems = new ArrayList<>();
        long totalExpense = 0;
        long totalIncome = 0;

        for (Map<String, Object> row : rows) {
            Integer type = (Integer) row.get("type");
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

        List<String> monthLabels = new ArrayList<>();
        List<Long> expenseList = new ArrayList<>();
        List<Long> incomeList = new ArrayList<>();

        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            monthLabels.add(ym.toString());

            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

            QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
            wrapper.select("type", "SUM(amount) as total")
                    .eq("book_id", bookId)
                    .ge("trans_time", start)
                    .lt("trans_time", end)
                    .groupBy("type");

            List<Map<String, Object>> rows = listMaps(wrapper);
            long exp = 0, inc = 0;
            for (Map<String, Object> row : rows) {
                Integer type = (Integer) row.get("type");
                Long total = row.get("total") != null ? ((Number) row.get("total")).longValue() : 0L;
                if (type != null && type == 0) exp = total;
                else if (type != null && type == 1) inc = total;
            }
            expenseList.add(exp);
            incomeList.add(inc);
        }

        return new TrendResponse(monthLabels, expenseList, incomeList);
    }

    private void validateBookOwnership(Long bookId) {
        AccountBook book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
    }
}
