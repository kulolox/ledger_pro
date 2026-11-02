package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.service.impl.AccountBookServiceImpl;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountBookServiceImpl Unit Tests")
class AccountBookServiceImplTest {

    @Mock
    AccountBookMapper accountBookMapper;

    @Captor
    ArgumentCaptor<AccountBook> bookCaptor;

    AccountBookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountBookServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", accountBookMapper);
        ReflectionTestUtils.setField(service, "entityClass", AccountBook.class);

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
    @DisplayName("listByOwner: 正常返回当前用户账本列表")
    void listByOwner_success() {
        AccountBook b1 = new AccountBook();
        b1.setId(101L);
        b1.setName("默认账本");
        b1.setOwnerId(1L);

        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(b1));

        List<AccountBookResponse> result = service.listByOwner();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("默认账本", result.get(0).name());
    }

    @Test
    @DisplayName("create: 名称为空抛出异常")
    void create_blankName_throwsException() {
        CreateAccountBookRequest req = new CreateAccountBookRequest("   ", "icon", 0);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("账本名称不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("create: 名称过长抛出异常")
    void create_nameTooLong_throwsException() {
        String longName = "a".repeat(51);
        CreateAccountBookRequest req = new CreateAccountBookRequest(longName, "icon", 0);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("账本名称长度不能超过50个字符", ex.getMessage());
    }

    @Test
    @DisplayName("create: 成功创建并进行 HTML 转义")
    void create_success_withHtmlSanitize() {
        CreateAccountBookRequest req = new CreateAccountBookRequest("<script>日常</script>", "📒", 0);

        when(accountBookMapper.insert(bookCaptor.capture())).thenAnswer(invocation -> {
            AccountBook arg = invocation.getArgument(0);
            arg.setId(201L);
            return 1;
        });

        AccountBookResponse res = service.create(req);
        assertNotNull(res);
        assertFalse(res.name().contains("<script>"));
        assertTrue(res.name().contains("&lt;script&gt;"));
        assertEquals("📒", res.coverIcon());
        assertEquals(1L, bookCaptor.getValue().getOwnerId());
    }

    @Test
    @DisplayName("create: 未提供coverIcon时默认空字符串兜底")
    void create_withoutCoverIcon_defaultsEmptyString() {
        CreateAccountBookRequest req = new CreateAccountBookRequest("默认账本", null, 0);

        when(accountBookMapper.insert(bookCaptor.capture())).thenAnswer(invocation -> {
            AccountBook arg = invocation.getArgument(0);
            arg.setId(202L);
            return 1;
        });

        AccountBookResponse res = service.create(req);
        assertNotNull(res);
        assertEquals("", res.coverIcon());
        assertEquals("", bookCaptor.getValue().getCoverIcon());
    }
}
