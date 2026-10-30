package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.service.impl.AccountServiceImpl;
import java.util.List;
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
@DisplayName("AccountServiceImpl Unit Tests")
class AccountServiceImplTest {

    @Mock
    AccountMapper accountMapper;

    @Mock
    AccountBookMapper accountBookMapper;

    AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(accountBookMapper);
        ReflectionTestUtils.setField(service, "baseMapper", accountMapper);
        ReflectionTestUtils.setField(service, "entityClass", Account.class);

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
    @DisplayName("listByBookId: bookId为null时抛出异常")
    void listByBookId_nullBookId_throwsException() {
        BizException ex = assertThrows(BizException.class, () -> service.listByBookId(null));
        assertEquals("账本ID不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("listByBookId: 账本不存在时抛出 BizException")
    void listByBookId_bookNotFound_throwsException() {
        when(accountBookMapper.selectById(100L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.listByBookId(100L));
        assertEquals("无权访问该账本", ex.getMessage());
    }

    @Test
    @DisplayName("listByBookId: 账本不属于当前用户时抛出 BizException")
    void listByBookId_notOwner_throwsException() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(2L); // Different owner
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        BizException ex = assertThrows(BizException.class, () -> service.listByBookId(100L));
        assertEquals("无权访问该账本", ex.getMessage());
    }

    @Test
    @DisplayName("listByBookId: 正常返回账本下的账户列表")
    void listByBookId_success() {
        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectById(100L)).thenReturn(book);

        Account a1 = new Account();
        a1.setId(10L);
        a1.setBookId(100L);
        a1.setName("现金");
        a1.setType(0);
        a1.setSortOrder(1);

        when(accountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(a1));

        List<AccountResponse> result = service.listByBookId(100L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("现金", result.get(0).name());
    }
}
