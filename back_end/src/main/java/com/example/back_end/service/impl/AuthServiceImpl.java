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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor  // 自动生成所有 final 字段的构造方法
@Slf4j
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements IAuthService {
    private final AccountBookMapper accountBookMapper;
    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;
    private final MailService mailService;
    private final ITokenBlacklistService tokenBlacklistService;

    private static final SecureRandom RANDOM = new SecureRandom();


    @Override
    @Transactional // 事务回滚注解（所有数据库操作要么都成功，要么都失败）
    public LoginResponse register(RegisterRequest req) {
        // 前端 RSA 公钥加密传输，后端私钥解密；仅解密后的明文参与 BCrypt 哈希
        String rawPassword = rsaUtils.decrypt(req.password());
        if (lambdaQuery().eq(User::getEmail, req.email()).count() > 0) {
            throw new BizException("邮箱已注册");
        }

        // 注册创建账号
        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNickname(req.nickname() != null ? SanitizeUtils.escapeHtml(req.nickname()) : "");
        save(user);

        // 注册时初始化默认数据：日常账本 + 现金账户（与用户数据同事务）
        AccountBook book = new AccountBook();
//        book.setId(generateBookId());
        book.setName("日常账本");
        book.setOwnerId(user.getId());
        book.setType(0);
        accountBookMapper.insert(book);

        // 注册创建默认现金账户
        Account account = new Account();
        account.setBookId(book.getId());
        account.setName("现金");
        account.setType(0);
        account.setIcon("💵");
        accountMapper.insert(account);

        // 根据注册数据生成token,refreshToken
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        log.info("用户注册成功 userId={} email={}", user.getId(), user.getEmail());
        // 返回登录数据
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // 解密 → 按邮箱查用户 → BCrypt 比对；用户不存在与密码错误返回同一提示，防账号枚举
        String rawPassword = rsaUtils.decrypt(req.password());
        User user = lambdaQuery().eq(User::getEmail, req.email()).one();
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BizException(401, "邮箱或密码错误");
        }
        // 根据登录数据生成token,refreshToken
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        log.info("用户登录成功 userId={} email={}", user.getId(), user.getEmail());
        // 返回登录数据
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // 登出 = 把 token 加入黑名单，TTL 取 token 剩余有效期；拉黑失败不阻塞登出
        boolean accessOk = false;
        boolean refreshOk = false;
        if (accessToken != null && jwtUtils.validateToken(accessToken)) {
            Date exp = jwtUtils.getExpiration(accessToken);
            long ttl = Duration.between(Instant.now(), exp.toInstant()).toSeconds();
            try {
                tokenBlacklistService.blacklist(accessToken, ttl);
                accessOk = true;
            } catch (Exception e) {
                log.error("access token 拉黑失败，token 将保留至自然过期", e);
            }
        }
        if (refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)) {
            Date exp = jwtUtils.getExpiration(refreshToken);
            long ttl = Duration.between(Instant.now(), exp.toInstant()).toSeconds();
            try {
                tokenBlacklistService.blacklist(refreshToken, ttl);
                refreshOk = true;
            } catch (Exception e) {
                log.error("refresh token 拉黑失败，token 将保留至自然过期", e);
            }
        }
        log.info("用户登出 accessTokenBlacklisted={} refreshTokenBlacklisted={}", accessOk, refreshOk);
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

        // 防频繁请求：验证码 5 分钟内不重发
        if (user.getResetCodeExpiresAt() != null
                && user.getResetCodeExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BizException("验证码已发送，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        // 邮件发送成功后落库验证码，发送失败不会产生不可用的验证码
        mailService.sendResetCode(email, code);

        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        updateById(user);
        log.info("重置验证码已发送 email={}", email);
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
        log.info("密码重置成功 email={}", email);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // access token 过期后用 refresh token 换发新 token 对；黑名单与用户有效性都需通过
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
        log.info("token 刷新成功 userId={}", userId);
        return new LoginResponse(newToken, newRefreshToken, userInfo);
    }

//    private Long generateBookId() {
//        long id;
//        do {
//            // 随机生产bookId,当bookId没有被占用就返回
//            id = ThreadLocalRandom.current().nextLong(10000000L, 100000000000L);
//        } while (accountBookMapper.selectById(id) != null);
//        return id;
//    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}
