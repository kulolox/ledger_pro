package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.back_end.common.BizException;
import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.dto.response.TrendResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.impl.StatisticsServiceImpl;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsServiceImpl Unit Tests")
class StatisticsServiceImplTest {

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    AccountBookMapper accountBookMapper;

    StatisticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StatisticsServiceImpl(accountBookMapper);
        ReflectionTestUtils.setField(service, "baseMapper", transactionMapper);
        ReflectionTestUtils.setField(service, "entityClass", Transaction.class);

        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public String getName() {
                return "1";
            }

            @Override
            public Object getPrincipal() {
                return 1L;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean b) {}

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return List.of();
            }
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("monthly: 月份格式不正确时抛出异常")
    void monthly_invalidMonthFormat_throwsException() {
        BizException ex = assertThrows(BizException.class, () -> service.monthly(100L, "2026-7"));
        assertEquals("月份格式不正确，应为 YYYY-MM", ex.getMessage());

        BizException ex2 = assertThrows(BizException.class, () -> service.monthly(100L, null));
        assertEquals("月份格式不正确，应为 YYYY-MM", ex2.getMessage());
    }

    @Test
    @DisplayName("monthly: 账本不存在或无权访问时抛出异常")
    void monthly_unauthorizedBook_throwsException() {
        when(accountBookMapper.selectById(100L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> service.monthly(100L, "2026-07"));
        assertEquals("无权访问该账本", ex.getMessage());

        AccountBook otherBook = new AccountBook();
        otherBook.setId(100L);
        otherBook.setOwnerId(999L);
        when(accountBookMapper.selectById(100L)).thenReturn(otherBook);
        BizException ex2 = assertThrows(BizException.class, () -> service.monthly(100L, "2026-07"));
        assertEquals("无权访问该账本", ex2.getMessage());
    }

    @Test
    @DisplayName("monthly: 正常分类聚合统计")
    void monthly_success() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        Map<String, Object> expenseRow = Map.of(
                "type", 0,
                "category_id", 10L,
                "total", 3500L,
                "count", 2L);

        Map<String, Object> incomeRow = Map.of(
                "type", 1,
                "category_id", 20L,
                "total", 10000L,
                "count", 1L);

        when(transactionMapper.sumByCategoryAndType(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expenseRow, incomeRow));

        StatisticsResponse res = service.monthly(100L, "2026-07");
        assertNotNull(res);
        assertEquals(3500L, res.total_expense());
        assertEquals(10000L, res.total_income());
        assertEquals(1, res.expense_by_category().size());
        assertEquals(10L, res.expense_by_category().get(0).category_id());
        assertEquals(1, res.income_by_category().size());
        assertEquals(20L, res.income_by_category().get(0).category_id());
    }

    @Test
    @DisplayName("monthly: 无数据时正确返回全0统计")
    void monthly_emptyData_returnsZeros() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        when(transactionMapper.sumByCategoryAndType(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        StatisticsResponse res = service.monthly(100L, "2026-07");
        assertNotNull(res);
        assertEquals(0L, res.total_expense());
        assertEquals(0L, res.total_income());
        assertTrue(res.expense_by_category().isEmpty());
        assertTrue(res.income_by_category().isEmpty());
    }

    @Test
    @DisplayName("yearly: 年份格式不正确时抛出异常")
    void yearly_invalidYearFormat_throwsException() {
        BizException ex = assertThrows(BizException.class, () -> service.yearly(100L, "26"));
        assertEquals("年份格式不正确，应为 YYYY", ex.getMessage());

        BizException ex2 = assertThrows(BizException.class, () -> service.yearly(100L, null));
        assertEquals("年份格式不正确，应为 YYYY", ex2.getMessage());
    }

    @Test
    @DisplayName("yearly: 正常按年分类聚合统计")
    void yearly_success() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        Map<String, Object> expenseRow = Map.of(
                "type", 0,
                "category_id", 10L,
                "total", 50000L,
                "count", 15L);

        Map<String, Object> incomeRow = Map.of(
                "type", 1,
                "category_id", 20L,
                "total", 120000L,
                "count", 12L);

        when(transactionMapper.sumByCategoryAndType(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(expenseRow, incomeRow));

        StatisticsResponse res = service.yearly(100L, "2026");
        assertNotNull(res);
        assertEquals(50000L, res.total_expense());
        assertEquals(120000L, res.total_income());
        assertEquals(1, res.expense_by_category().size());
        assertEquals(10L, res.expense_by_category().get(0).category_id());
        assertEquals(15L, res.expense_by_category().get(0).count());
        assertEquals(1, res.income_by_category().size());
        assertEquals(20L, res.income_by_category().get(0).category_id());
    }

    @Test
    @DisplayName("trend: 正确生成连续月份趋势数据")
    void trend_success() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        String currentYm = YearMonth.now().toString();
        Map<String, Object> row = Map.of("ym", currentYm, "type", 0, "total", 5000L);

        when(transactionMapper.sumByMonthAndType(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(row));

        TrendResponse res = service.trend(100L, 6);
        assertNotNull(res);
        assertEquals(6, res.months().size());
        assertEquals(6, res.expense().size());
        assertEquals(6, res.income().size());
        assertEquals(5000L, res.expense().get(5));
        assertEquals(0L, res.income().get(5));
    }
}
