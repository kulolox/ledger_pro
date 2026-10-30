package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.User;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.impl.UserServiceImpl;
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
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    UserMapper userMapper;

    UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        ReflectionTestUtils.setField(service, "entityClass", User.class);

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
    @DisplayName("getInfo: 用户不存在抛出异常")
    void getInfo_userNotFound_throwsException() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.getInfo());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("getInfo: 成功返回用户信息")
    void getInfo_success() {
        User user = new User();
        user.setId(1L);
        user.setNickname("张三");
        user.setEmail("zhangsan@test.com");
        user.setAvatarUrl("avatar.png");
        user.setPhone("13800138000");

        when(userMapper.selectById(1L)).thenReturn(user);

        UserInfoResponse res = service.getInfo();
        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("张三", res.nickname());
        assertEquals("zhangsan@test.com", res.email());
    }

    @Test
    @DisplayName("updateInfo: 手机号格式不正确抛出异常")
    void updateInfo_invalidPhone_throwsException() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest("张三", "avatar.png", "12345");
        BizException ex = assertThrows(BizException.class, () -> service.updateInfo(req));
        assertEquals("手机号格式不正确", ex.getMessage());
    }

    @Test
    @DisplayName("updateInfo: 成功更新并防XSS注入")
    void updateInfo_success_withXssSanitization() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest("<img src=x onerror=alert(1)>", "avatar.png", "13800138000");
        UserInfoResponse res = service.updateInfo(req);

        assertNotNull(res);
        assertFalse(res.nickname().contains("<img"));
        assertTrue(res.nickname().contains("&lt;img"));
        verify(userMapper).updateById(user);
    }
}
