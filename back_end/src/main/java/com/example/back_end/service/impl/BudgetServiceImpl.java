package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CreateBudgetRequest;
import com.example.back_end.dto.request.UpdateBudgetRequest;
import com.example.back_end.dto.response.BudgetResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Budget;
import com.example.back_end.entity.Transaction;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.BudgetMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.IBudgetService;
import com.example.back_end.utils.AuthUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements IBudgetService {

    private final AccountBookMapper accountBookMapper;
    private final TransactionMapper transactionMapper;

    public BudgetServiceImpl(AccountBookMapper accountBookMapper, TransactionMapper transactionMapper) {
        this.accountBookMapper = accountBookMapper;
        this.transactionMapper = transactionMapper;
    }

    private void validateBookOwnership(Long bookId) {
        var book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
    }

    public List<BudgetResponse> listByBookId(Long bookId, String month) {
        validateBookOwnership(bookId);

        YearMonth ym = YearMonth.parse(month);
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Budget> budgets = lambdaQuery()
                .eq(Budget::getBookId, bookId)
                .eq(Budget::getStatus, 1)
                .list();

        return budgets.stream().map(b -> {
            Long spent = getSpent(bookId, b.getCategoryId(), monthStart, monthEnd);
            int percent = b.getAmount() > 0 ? (int) Math.min(100, spent * 100 / b.getAmount()) : 0;
            return toResponse(b, spent, percent);
        }).toList();
    }

    private Long getSpent(Long bookId, Long categoryId, LocalDateTime start, LocalDateTime end) {
        var wrapper = new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getBookId, bookId)
                .eq(Transaction::getDeleted, 0)
                .eq(Transaction::getType, 0)
                .ge(Transaction::getTransTime, start)
                .lt(Transaction::getTransTime, end);
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }
        Long result = transactionMapper.selectSumAmount(wrapper);
        return result != null ? result : 0L;
    }

    @Transactional
    public BudgetResponse create(CreateBudgetRequest req) {
        validateBookOwnership(req.book_id());
        Long userId = AuthUtils.getCurrentUserId();

        Budget b = new Budget();
        b.setBookId(req.book_id());
        b.setUserId(userId);
        b.setCategoryId(req.category_id());
        b.setAmount(req.amount());
        b.setPeriodType(req.period_type());
        b.setStartDate(LocalDate.parse(req.start_date()));
        b.setCarryOver(req.carry_over() != null ? req.carry_over() : 0);
        b.setStatus(1);
        save(b);
        return toResponse(b, 0L, 0);
    }

    @Transactional
    public BudgetResponse update(Long id, UpdateBudgetRequest req) {
        Budget b = getById(id);
        if (b == null) throw new BizException("预算不存在");
        validateBookOwnership(b.getBookId());

        if (req.category_id() != null) b.setCategoryId(req.category_id());
        if (req.amount() != null) b.setAmount(req.amount());
        if (req.period_type() != null) b.setPeriodType(req.period_type());
        if (req.start_date() != null) b.setStartDate(LocalDate.parse(req.start_date()));
        if (req.carry_over() != null) b.setCarryOver(req.carry_over());
        if (req.status() != null) b.setStatus(req.status());
        b.setUpdatedAt(LocalDateTime.now());
        updateById(b);

        YearMonth ym = YearMonth.from(b.getStartDate());
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.plusMonths(1).atDay(1).atStartOfDay();
        Long spent = getSpent(b.getBookId(), b.getCategoryId(), monthStart, monthEnd);
        int percent = b.getAmount() > 0 ? (int) Math.min(100, spent * 100 / b.getAmount()) : 0;
        return toResponse(b, spent, percent);
    }

    @Transactional
    public void delete(Long id, Long bookId) {
        validateBookOwnership(bookId);
        Budget b = getById(id);
        if (b == null || !b.getBookId().equals(bookId)) throw new BizException("预算不存在");
        removeById(id);
    }

    private BudgetResponse toResponse(Budget b, Long spent, int percent) {
        return new BudgetResponse(
                b.getId(), b.getBookId(), b.getCategoryId(), b.getAmount(),
                b.getPeriodType(), b.getStartDate().toString(),
                b.getCarryOver(), b.getStatus(), spent, percent);
    }
}
