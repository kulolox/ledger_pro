package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.IAuthService;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.RsaUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements IAuthService {
    private final AccountBookMapper accountBookMapper;
    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;
    private final MailService mailService;
    private final ITokenBlacklistService tokenBlacklistService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthServiceImpl(
            AccountBookMapper accountBookMapper,
            AccountMapper accountMapper,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            RsaUtils rsaUtils,
            MailService mailService,
            ITokenBlacklistService tokenBlacklistService) {
        this.accountBookMapper = accountBookMapper;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.rsaUtils = rsaUtils;
        this.mailService = mailService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        String rawPassword = rsaUtils.decrypt(req.password());
        if (lambdaQuery().eq(User::getEmail, req.email()).count() > 0) {
            throw new BizException("邮箱已注册");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNickname(req.nickname() != null ? SanitizeUtils.escapeHtml(req.nickname()) : "");
        save(user);

        AccountBook book = new AccountBook();
        book.setId(generateBookId());
        book.setName("日常账本");
        book.setOwnerId(user.getId());
        book.setType(0);
        accountBookMapper.insert(book);

        Account account = new Account();
        account.setBookId(book.getId());
        account.setName("现金");
        account.setType(0);
        account.setIcon("💵");
        accountMapper.insert(account);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        String rawPassword = rsaUtils.decrypt(req.password());
        User user = lambdaQuery().eq(User::getEmail, req.email()).one();
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BizException(401, "邮箱或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && jwtUtils.validateToken(accessToken)) {
            Date exp = jwtUtils.getExpiration(accessToken);
            long ttl = Duration.between(Instant.now(), exp.toInstant()).toSeconds();
            tokenBlacklistService.blacklist(accessToken, ttl);
        }
        if (refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)) {
            Date exp = jwtUtils.getExpiration(refreshToken);
            long ttl = Duration.between(Instant.now(), exp.toInstant()).toSeconds();
            tokenBlacklistService.blacklist(refreshToken, ttl);
        }
    }

    @Override
    public void sendResetCode(String email) {
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new BizException("邮箱格式不正确");
        }

        User user = lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            throw new BizException("该邮箱未注册");
        }

        if (user.getResetCodeExpiresAt() != null
                && user.getResetCodeExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BizException("验证码已发送，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        mailService.sendResetCode(email, code);

        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        updateById(user);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        if (email == null || code == null || newPassword == null) {
            throw new BizException("参数不完整");
        }

        User user = lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (user.getResetCode() == null
                || user.getResetCodeExpiresAt() == null
                || user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())
                || !MessageDigest.isEqual(
                        user.getResetCode().getBytes(StandardCharsets.UTF_8), code.getBytes(StandardCharsets.UTF_8))) {
            throw new BizException("验证码错误或已过期");
        }

        String rawPassword = rsaUtils.decrypt(newPassword);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        updateById(user);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtUtils.validateRefreshToken(refreshToken)) {
            throw new BizException(401, "refresh_token无效或已过期，请重新登录");
        }
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BizException(401, "refresh_token已被注销，请重新登录");
        }
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String email = jwtUtils.getEmailFromToken(refreshToken);
        var user = getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(401, "用户不存在");
        }
        String newToken = jwtUtils.generateToken(userId, email);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, email);
        var userInfo = new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
        return new LoginResponse(newToken, newRefreshToken, userInfo);
    }

    private Long generateBookId() {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong(10000000L, 100000000000L);
        } while (accountBookMapper.selectById(id) != null);
        return id;
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}
