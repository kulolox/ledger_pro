package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.service.impl.TransactionServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl.update()")
class TransactionServiceUpdateTest {

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    AccountBookMapper accountBookMapper;

    @Mock
    IUserCategoryService userCategoryService;

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache cache;

    @Captor
    ArgumentCaptor<Transaction> txCaptor;

    TransactionServiceImpl service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(userCategoryService, accountBookMapper, cacheManager);
        ReflectionTestUtils.setField(service, "baseMapper", transactionMapper);

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
                return java.util.List.of();
            }
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateAmountAndRemark() {
        when(cacheManager.getCache("transactions")).thenReturn(cache);

        var book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        var existing = new Transaction();
        existing.setId(1L);
        existing.setBookId(100L);
        existing.setAmount(1000);
        existing.setType(0);
        existing.setCategoryId(2L);
        existing.setRemark("原备注");
        existing.setTransTime(LocalDateTime.of(2026, 7, 23, 10, 0));
        existing.setAccountId(1L);
        when(transactionMapper.selectById(1L)).thenReturn(existing);

        var req = new TransactionRequest(100L, 0, 2000, 1L, null, 2L, "新备注", "2026-07-23T12:00:00");

        TransactionResponse result = service.update(1L, req);

        assertAll(
                () -> assertEquals(2000, result.amount()),
                () -> assertEquals("新备注", result.remark()),
                () -> assertEquals(0, result.type()),
                () -> assertEquals(1L, result.id()));

        verify(transactionMapper).updateById(txCaptor.capture());
        assertEquals(2000, txCaptor.getValue().getAmount());
        assertEquals("新备注", txCaptor.getValue().getRemark());
        verify(cache, atLeastOnce()).evict(any());
    }

    @Test
    void shouldThrowWhenTransactionNotFound() {
        when(transactionMapper.selectById(999L)).thenReturn(null);

        var req = new TransactionRequest(100L, 0, 2000, 1L, null, 2L, "", "2026-07-23T12:00:00");

        assertThrows(BizException.class, () -> service.update(999L, req));
    }

    @Test
    void shouldThrowWhenBookNotOwned() {
        var existing = new Transaction();
        existing.setId(1L);
        existing.setBookId(100L);
        existing.setAmount(1000);
        existing.setType(0);
        existing.setCategoryId(2L);
        existing.setRemark("");
        existing.setTransTime(LocalDateTime.now());
        existing.setAccountId(1L);
        when(transactionMapper.selectById(1L)).thenReturn(existing);
        when(accountBookMapper.selectById(100L)).thenReturn(createBook(100L, 999L));

        var req = new TransactionRequest(100L, 0, 2000, 1L, null, 2L, "", "2026-07-23T12:00:00");

        assertThrows(BizException.class, () -> service.update(1L, req));
    }

    private AccountBook createBook(Long id, Long ownerId) {
        var book = new AccountBook();
        book.setId(id);
        book.setOwnerId(ownerId);
        return book;
    }
}
