package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.CaptchaResponse;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.service.IVerificationCodeService;
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

    @Mock
    IVerificationCodeService verificationCodeService;

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
                tokenBlacklistService,
                verificationCodeService);
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        ReflectionTestUtils.setField(service, "entityClass", User.class);
    }

    @Test
    @DisplayName("getCaptcha: 成功生成图形验证码并存入缓存")
    void getCaptcha_success() {
        CaptchaResponse resp = service.getCaptcha();
        assertNotNull(resp);
        assertNotNull(resp.captchaKey());
        assertTrue(resp.captchaBase64().startsWith("data:image/png;base64,"));
        verify(verificationCodeService).saveCaptcha(eq(resp.captchaKey()), anyString(), eq(120L));
    }

    @Test
    @DisplayName("sendRegisterCode: 邮箱已注册时抛出异常")
    void sendRegisterCode_emailExists_throwsException() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        BizException ex =
                assertThrows(BizException.class, () -> service.sendRegisterCode("test@test.com", "key123", "code123"));
        assertEquals("该邮箱已被注册", ex.getMessage());
    }

    @Test
    @DisplayName("sendRegisterCode: 图形验证码错误或失效时抛出异常")
    void sendRegisterCode_invalidCaptcha_throwsException() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(verificationCodeService.verifyAndInvalidateCaptcha("key123", "wrong"))
                .thenReturn(false);

        BizException ex =
                assertThrows(BizException.class, () -> service.sendRegisterCode("test@test.com", "key123", "wrong"));
        assertEquals("图形验证码错误或已失效", ex.getMessage());
    }

    @Test
    @DisplayName("sendRegisterCode: 冷却时间内抛出异常")
    void sendRegisterCode_inCooldown_throwsException() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(verificationCodeService.verifyAndInvalidateCaptcha("key123", "valid"))
                .thenReturn(true);
        when(verificationCodeService.isRegisterCooldown("test@test.com")).thenReturn(true);

        BizException ex =
                assertThrows(BizException.class, () -> service.sendRegisterCode("test@test.com", "key123", "valid"));
        assertEquals("验证码发送太频繁，请稍后再试", ex.getMessage());
    }

    @Test
    @DisplayName("sendRegisterCode: 成功发送并写入缓存")
    void sendRegisterCode_success() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(verificationCodeService.verifyAndInvalidateCaptcha("key123", "valid"))
                .thenReturn(true);
        when(verificationCodeService.isRegisterCooldown("test@test.com")).thenReturn(false);

        service.sendRegisterCode("test@test.com", "key123", "valid");

        verify(mailService).sendRegisterCode(eq("test@test.com"), anyString());
        verify(verificationCodeService).saveRegisterCode(eq("test@test.com"), anyString(), eq(600L));
        verify(verificationCodeService).setRegisterCooldown(eq("test@test.com"), eq(60L));
    }

    @Test
    @DisplayName("register: 邮箱格式不正确时抛出异常")
    void register_invalidEmail_throwsException() {
        RegisterRequest req = new RegisterRequest("invalid-email", "pwd", "nick", "123456");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("邮箱格式不正确", ex.getMessage());
    }

    @Test
    @DisplayName("register: 邮箱验证码错误或失效时抛出异常")
    void register_invalidCode_throwsException() {
        when(verificationCodeService.verifyAndInvalidateRegisterCode("test@test.com", "123456"))
                .thenReturn(false);
        RegisterRequest req = new RegisterRequest("test@test.com", "encPwd", "nick", "123456");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("邮箱验证码错误或已过期", ex.getMessage());
    }

    @Test
    @DisplayName("register: 密码过短时抛出异常")
    void register_shortPassword_throwsException() {
        when(verificationCodeService.verifyAndInvalidateRegisterCode("test@test.com", "123456"))
                .thenReturn(true);
        when(rsaUtils.decrypt("encPwd")).thenReturn("123");
        RegisterRequest req = new RegisterRequest("test@test.com", "encPwd", "nick", "123456");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("密码长度必须在6-32位之间", ex.getMessage());
    }

    @Test
    @DisplayName("register: 邮箱已存在时抛出异常")
    void register_emailExists_throwsException() {
        when(verificationCodeService.verifyAndInvalidateRegisterCode("existing@test.com", "123456"))
                .thenReturn(true);
        when(rsaUtils.decrypt("encPwd")).thenReturn("123456");
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        RegisterRequest req = new RegisterRequest("existing@test.com", "encPwd", "nick", "123456");
        BizException ex = assertThrows(BizException.class, () -> service.register(req));
        assertEquals("邮箱已注册", ex.getMessage());
    }

    @Test
    @DisplayName("register: 注册成功自动创建默认账本和账户并返回Token")
    void register_success() {
        when(verificationCodeService.verifyAndInvalidateRegisterCode("new@test.com", "123456"))
                .thenReturn(true);
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

        RegisterRequest req = new RegisterRequest("new@test.com", "encPwd", "新用户", "123456");
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
        user.setEmail("user@test.com");
        user.setPasswordHash("hashedActualPwd");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrongPwd", "hashedActualPwd")).thenReturn(false);

        LoginRequest req = new LoginRequest("user@test.com", "wrongEncPwd");
        BizException ex = assertThrows(BizException.class, () -> service.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("邮箱或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("login: 用户不存在同样抛出 401 统一异常")
    void login_userNotFound_throws401() {
        when(rsaUtils.decrypt("anyEncPwd")).thenReturn("anyPwd");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        LoginRequest req = new LoginRequest("nobody@test.com", "anyEncPwd");
        BizException ex = assertThrows(BizException.class, () -> service.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("邮箱或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("login: 密码正确返回 Token 和用户信息")
    void login_success() {
        when(rsaUtils.decrypt("validEncPwd")).thenReturn("correctPwd");
        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");
        user.setPasswordHash("hashedPwd");
        user.setNickname("老王");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("correctPwd", "hashedPwd")).thenReturn(true);
        when(jwtUtils.generateToken(10L, "user@test.com")).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(10L, "user@test.com")).thenReturn("refresh-token");

        LoginRequest req = new LoginRequest("user@test.com", "validEncPwd");
        LoginResponse res = service.login(req);

        assertNotNull(res);
        assertEquals("access-token", res.token());
        assertEquals("refresh-token", res.refreshToken());
        assertEquals("老王", res.userInfo().nickname());
    }

    @Test
    @DisplayName("logout: access_token 和 refresh_token 均加入黑名单")
    void logout_validTokens_blacklisted() {
        when(jwtUtils.validateToken("validAccess")).thenReturn(true);
        when(jwtUtils.validateRefreshToken("validRefresh")).thenReturn(true);
        Date future = new Date(System.currentTimeMillis() + 60000);
        when(jwtUtils.getExpiration("validAccess")).thenReturn(future);
        when(jwtUtils.getExpiration("validRefresh")).thenReturn(future);

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
