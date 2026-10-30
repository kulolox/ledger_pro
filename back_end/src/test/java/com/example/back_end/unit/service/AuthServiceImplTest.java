package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.service.impl.AuthServiceImpl;
import com.example.back_end.service.impl.MailService;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.RsaUtils;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    UserMapper userMapper;

    @Mock
    AccountBookMapper accountBookMapper;

    @Mock
    AccountMapper accountMapper;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    JwtUtils jwtUtils;

    @Mock
    RsaUtils rsaUtils;

    @Mock
    MailService mailService;

    @Mock
    ITokenBlacklistService tokenBlacklistService;

    @Captor
    ArgumentCaptor<User> userCaptor;

    AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                accountBookMapper,
                accountMapper,
                passwordEncoder,
                jwtUtils,
                rsaUtils,
                mailService,
                tokenBlacklistService);
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        ReflectionTestUtils.setField(service, "entityClass", User.class);
    }

    @Test
    @DisplayName("register: 邮箱格式不正确时抛出异常")
    void register_invalidEmail_throwsException() {
        RegisterRequest req = new RegisterRequest("invalid-email", "pwd", "nick");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("邮箱格式不正确", ex.getMessage());
    }

    @Test
    @DisplayName("register: 密码过短时抛出异常")
    void register_shortPassword_throwsException() {
        when(rsaUtils.decrypt("encPwd")).thenReturn("123");
        RegisterRequest req = new RegisterRequest("test@test.com", "encPwd", "nick");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("密码长度必须在6-32位之间", ex.getMessage());
    }

    @Test
    @DisplayName("register: 邮箱已存在时抛出异常")
    void register_emailExists_throwsException() {
        when(rsaUtils.decrypt("encPwd")).thenReturn("123456");
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        RegisterRequest req = new RegisterRequest("existing@test.com", "encPwd", "nick");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("邮箱已注册", ex.getMessage());
    }

    @Test
    @DisplayName("register: 注册成功自动创建默认账本和账户并返回Token")
    void register_success() {
        when(rsaUtils.decrypt("encPwd")).thenReturn("123456");
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return 1;
        });
        when(accountBookMapper.insert(any(AccountBook.class))).thenAnswer(invocation -> {
            AccountBook b = invocation.getArgument(0);
            b.setId(88L);
            return 1;
        });
        when(jwtUtils.generateToken(99L, "new@test.com")).thenReturn("access-token-123");
        when(jwtUtils.generateRefreshToken(99L, "new@test.com")).thenReturn("refresh-token-123");

        RegisterRequest req = new RegisterRequest("new@test.com", "encPwd", "新用户");
        LoginResponse res = service.register(req);

        assertNotNull(res);
        assertEquals("access-token-123", res.token());
        assertEquals("refresh-token-123", res.refreshToken());
        assertEquals("new@test.com", res.userInfo().email());
        verify(accountBookMapper).insert(any(AccountBook.class));
        verify(accountMapper).insert(any(Account.class));
    }

    @Test
    @DisplayName("login: 密码错误抛出 401 统一异常")
    void login_wrongPassword_throws401() {
        when(rsaUtils.decrypt("wrongEncPwd")).thenReturn("wrongPwd");
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPasswordHash("correctHash");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrongPwd", "correctHash")).thenReturn(false);

        LoginRequest req = new LoginRequest("user@test.com", "wrongEncPwd");
        BizException ex = assertThrows(BizException.class, () -> service.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("邮箱或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("login: 用户不存在抛出 401 统一异常")
    void login_userNotFound_throws401() {
        when(rsaUtils.decrypt("anyPwd")).thenReturn("pwd");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        LoginRequest req = new LoginRequest("unknown@test.com", "anyPwd");
        BizException ex = assertThrows(BizException.class, () -> service.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("邮箱或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("login: 验证通过返回登录信息")
    void login_success() {
        when(rsaUtils.decrypt("correctEncPwd")).thenReturn("secret123");
        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");
        user.setNickname("用户10");
        user.setPasswordHash("hashedSecret");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("secret123", "hashedSecret")).thenReturn(true);
        when(jwtUtils.generateToken(10L, "user@test.com")).thenReturn("access_token");
        when(jwtUtils.generateRefreshToken(10L, "user@test.com")).thenReturn("refresh_token");

        LoginRequest req = new LoginRequest("user@test.com", "correctEncPwd");
        LoginResponse res = service.login(req);

        assertNotNull(res);
        assertEquals("access_token", res.token());
        assertEquals(10L, res.userInfo().id());
    }

    @Test
    @DisplayName("logout: 拉黑有效token")
    void logout_blacklistsValidTokens() {
        when(jwtUtils.validateToken("validAccess")).thenReturn(true);
        when(jwtUtils.getExpiration("validAccess")).thenReturn(new Date(System.currentTimeMillis() + 60000));
        when(jwtUtils.validateRefreshToken("validRefresh")).thenReturn(true);
        when(jwtUtils.getExpiration("validRefresh")).thenReturn(new Date(System.currentTimeMillis() + 120000));

        service.logout("validAccess", "validRefresh");

        verify(tokenBlacklistService).blacklist(eq("validAccess"), anyLong());
        verify(tokenBlacklistService).blacklist(eq("validRefresh"), anyLong());
    }

    @Test
    @DisplayName("sendResetCode: 5分钟内防刷校验")
    void sendResetCode_cooldownActive_throwsException() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(3));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        BizException ex = assertThrows(BizException.class, () -> service.sendResetCode("user@test.com"));
        assertEquals("验证码已发送，请稍后再试", ex.getMessage());
    }

    @Test
    @DisplayName("sendResetCode: 正常发送重置码")
    void sendResetCode_success() {
        User user = new User();
        user.setId(5L);
        user.setEmail("user@test.com");
        user.setResetCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        service.sendResetCode("user@test.com");

        verify(mailService).sendResetCode(eq("user@test.com"), anyString());
        verify(userMapper).updateById(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getResetCode());
        assertNotNull(userCaptor.getValue().getResetCodeExpiresAt());
    }

    @Test
    @DisplayName("resetPassword: 验证码错误或过期抛出异常")
    void resetPassword_invalidCode_throwsException() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setResetCode("123456");
        user.setResetCodeExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        BizException ex =
                assertThrows(BizException.class, () -> service.resetPassword("user@test.com", "123456", "encNewPwd"));
        assertEquals("验证码错误或已过期", ex.getMessage());
    }

    @Test
    @DisplayName("resetPassword: 成功重置密码")
    void resetPassword_success() {
        User user = new User();
        user.setId(5L);
        user.setEmail("user@test.com");
        user.setResetCode("654321");
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(3));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(rsaUtils.decrypt("encNewPwd")).thenReturn("newPassword123");
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

        service.resetPassword("user@test.com", "654321", "encNewPwd");

        verify(userMapper).updateById(userCaptor.capture());
        assertNull(userCaptor.getValue().getResetCode());
        assertNull(userCaptor.getValue().getResetCodeExpiresAt());
        assertEquals("newHashedPassword", userCaptor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("refresh: refresh_token被拉黑时拒绝刷新")
    void refresh_blacklistedToken_throws401() {
        when(jwtUtils.validateRefreshToken("blacklistedToken")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("blacklistedToken")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> service.refresh("blacklistedToken"));
        assertEquals(401, ex.getCode());
        assertEquals("refresh_token已被注销，请重新登录", ex.getMessage());
    }

    @Test
    @DisplayName("refresh: 成功换发新 Token 对")
    void refresh_success() {
        when(jwtUtils.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("validRefreshToken")).thenReturn(false);
        when(jwtUtils.getUserIdFromToken("validRefreshToken")).thenReturn(12L);
        when(jwtUtils.getEmailFromToken("validRefreshToken")).thenReturn("user12@test.com");

        User user = new User();
        user.setId(12L);
        user.setEmail("user12@test.com");
        user.setDeleted(0);
        when(userMapper.selectById(12L)).thenReturn(user);

        when(jwtUtils.generateToken(12L, "user12@test.com")).thenReturn("new-access");
        when(jwtUtils.generateRefreshToken(12L, "user12@test.com")).thenReturn("new-refresh");

        LoginResponse res = service.refresh("validRefreshToken");
        assertNotNull(res);
        assertEquals("new-access", res.token());
        assertEquals("new-refresh", res.refreshToken());
        assertEquals(12L, res.userInfo().id());
    }
}
