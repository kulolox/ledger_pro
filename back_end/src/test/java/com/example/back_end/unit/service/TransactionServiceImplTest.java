package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.service.impl.TransactionServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
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
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

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

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(userCategoryService, accountBookMapper, cacheManager);
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
    @DisplayName("create: 金额小于等于0抛出异常")
    void create_invalidAmount_throwsException() {
        TransactionRequest req = new TransactionRequest(100L, 0, 0, 1L, null, 2L, "备注", "2026-07-23 10:00:00");
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("交易金额必须大于0", ex.getMessage());

        TransactionRequest req2 = new TransactionRequest(100L, 0, -500, 1L, null, 2L, "备注", "2026-07-23 10:00:00");
        BizException ex2 = assertThrows(BizException.class, () -> service.create(req2));
        assertEquals("交易金额必须大于0", ex2.getMessage());
    }

    @Test
    @DisplayName("create: 交易类型非法抛出异常")
    void create_invalidType_throwsException() {
        TransactionRequest req = new TransactionRequest(100L, 99, 1000, 1L, null, 2L, "备注", "2026-07-23 10:00:00");
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("无效的交易类型", ex.getMessage());
    }

    @Test
    @DisplayName("create: 账本不属于当前用户抛出异常")
    void create_notOwner_throwsException() {
        when(accountBookMapper.selectById(100L)).thenReturn(null);
        TransactionRequest req = new TransactionRequest(100L, 0, 1000, 1L, null, 2L, "备注", "2026-07-23 10:00:00");
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("无权访问该账本", ex.getMessage());
    }

    @Test
    @DisplayName("create: 成功记账并转义XSS和驱逐缓存")
    void create_success() {
        when(cacheManager.getCache("transactions")).thenReturn(cache);
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(501L);
            return 1;
        });

        TransactionRequest req =
                new TransactionRequest(100L, 0, 2550, 1L, null, 2L, "<script>午餐</script>", "2026-07-23 12:30:00");
        TransactionResponse res = service.create(req);

        assertNotNull(res);
        assertEquals(501L, res.id());
        assertEquals(2550, res.amount());
        assertTrue(res.remark().contains("&lt;script&gt;"));
        verify(cache, atLeastOnce()).evict(any());
    }

    @Test
    @DisplayName("update: 越权防御(IDOR) - 用户试图修改非自身账本的流水应被拦截")
    void update_idorAttack_forbidden() {
        Transaction victimTx = new Transaction();
        victimTx.setId(999L);
        victimTx.setBookId(200L); // 属于用户2的账本200
        victimTx.setAmount(1000);
        when(transactionMapper.selectById(999L)).thenReturn(victimTx);

        // 用户2的账本，当前用户1无权访问
        AccountBook victimBook = new AccountBook();
        victimBook.setId(200L);
        victimBook.setOwnerId(2L);
        when(accountBookMapper.selectById(200L)).thenReturn(victimBook);

        // 攻击者传了自己的 book_id: 100L
        TransactionRequest req = new TransactionRequest(100L, 0, 2000, 1L, null, 2L, "篡改", "2026-07-23 12:00:00");

        BizException ex = assertThrows(BizException.class, () -> service.update(999L, req));
        assertEquals("无权访问该账本", ex.getMessage());
        verify(transactionMapper, never()).updateById(any(Transaction.class));
    }

    @Test
    @DisplayName("delete: 成功软删除流水并驱逐缓存")
    void delete_success() {
        when(cacheManager.getCache("transactions")).thenReturn(cache);
        Transaction tx = new Transaction();
        tx.setId(123L);
        tx.setBookId(100L);
        tx.setDeleted(0);
        tx.setTransTime(LocalDateTime.of(2026, 7, 10, 8, 0));
        when(transactionMapper.selectById(123L)).thenReturn(tx);

        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        service.delete(123L);

        verify(transactionMapper).updateById(txCaptor.capture());
        assertEquals(1, txCaptor.getValue().getDeleted());
        verify(cache, atLeastOnce()).evict(any());
    }

    @Test
    @DisplayName("batchDelete: 空ids抛出异常")
    void batchDelete_emptyIds_throwsException() {
        BatchDeleteRequest req = new BatchDeleteRequest(List.of());
        BizException ex = assertThrows(BizException.class, () -> service.batchDelete(req));
        assertEquals("ids 不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("batchDelete: 仅软删除属于当前用户账本的记录")
    void batchDelete_onlyDeletesOwned() {
        when(cacheManager.getCache("transactions")).thenReturn(cache);
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));

        Transaction tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setBookId(100L);
        tx1.setTransTime(LocalDateTime.now());

        Transaction tx2 = new Transaction();
        tx2.setId(2L);
        tx2.setBookId(200L); // Not owned
        tx2.setTransTime(LocalDateTime.now());

        when(transactionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(tx1, tx2));
        when(transactionMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        BatchDeleteRequest req = new BatchDeleteRequest(List.of(1L, 2L));
        int deleted = service.batchDelete(req);

        assertEquals(1, deleted);
        verify(cache, atLeastOnce()).evict(any());
    }

    @Test
    @DisplayName("create: 支持非补零日期格式 (如 2026-11-1 12:30:00)")
    void create_singleDigitMonthOrDay_success() {
        when(cacheManager.getCache("transactions")).thenReturn(cache);
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setId(601L);
            return 1;
        });

        TransactionRequest req = new TransactionRequest(100L, 0, 2550, 1L, null, 2L, "午餐", "2026-11-1 12:30:00");
        TransactionResponse res = service.create(req);

        assertNotNull(res);
        assertEquals(601L, res.id());
        assertTrue(res.trans_time().startsWith("2026-11-01T12:30"));
    }
}
