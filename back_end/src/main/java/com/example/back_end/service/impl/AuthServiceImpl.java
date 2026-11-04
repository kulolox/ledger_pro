package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.CaptchaResponse;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.IAuthService;
import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.service.IVerificationCodeService;
import com.example.back_end.utils.CaptchaUtil;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements IAuthService {
    private final AccountBookMapper accountBookMapper;
    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;
    private final MailService mailService;
    private final ITokenBlacklistService tokenBlacklistService;
    private final IVerificationCodeService verificationCodeService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$";

    @Override
    public CaptchaResponse getCaptcha() {
        CaptchaUtil.CaptchaItem item = CaptchaUtil.generateCaptcha();
        String captchaKey = UUID.randomUUID().toString();
        // 图形验证码 2 分钟有效
        verificationCodeService.saveCaptcha(captchaKey, item.code(), 120);
        return new CaptchaResponse(captchaKey, item.imageBase64());
    }

    @Override
    public void sendRegisterCode(String email, String captchaKey, String captchaCode) {
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new BizException("邮箱格式不正确");
        }

        // 校验邮箱是否已被注册
        if (lambdaQuery().eq(User::getEmail, email.trim()).count() > 0) {
            throw new BizException("该邮箱已被注册");
        }

        // 前置校验人机图形验证码（一次性核销）
        if (captchaKey == null
                || captchaCode == null
                || !verificationCodeService.verifyAndInvalidateCaptcha(captchaKey, captchaCode)) {
            throw new BizException("图形验证码错误或已失效");
        }

        // 检查 60 秒发信冷却
        if (verificationCodeService.isRegisterCooldown(email)) {
            throw new BizException("验证码发送太频繁，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        // 发送邮件
        mailService.sendRegisterCode(email, code);

        // 验证码存入缓存（10 分钟有效），并设置 60 秒发信冷却
        verificationCodeService.saveRegisterCode(email, code, 600);
        verificationCodeService.setRegisterCooldown(email, 60);

        log.info("注册验证码已发送 email={}", email);
    }

    @Override
    public void sendResetCode(String email, String captchaKey, String captchaCode) {
        if (captchaKey == null
                || captchaCode == null
                || !verificationCodeService.verifyAndInvalidateCaptcha(captchaKey, captchaCode)) {
            throw new BizException("图形验证码错误或已失效");
        }
        sendResetCode(email);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (req.email() == null || !req.email().matches(EMAIL_REGEX)) {
            throw new BizException("邮箱格式不正确");
        }

        // 校验邮箱验证码（一次性核销）
        if (req.code() == null || !verificationCodeService.verifyAndInvalidateRegisterCode(req.email(), req.code())) {
            throw new BizException("邮箱验证码错误或已过期");
        }

        // 前端 RSA 公钥加密传输，后端私钥解密；仅解密后的明文参与 BCrypt 哈希
        String rawPassword = rsaUtils.decrypt(req.password());
        if (rawPassword == null || rawPassword.length() < 6 || rawPassword.length() > 32) {
            throw new BizException("密码长度必须在6-32位之间");
        }

        if (lambdaQuery().eq(User::getEmail, req.email().trim()).count() > 0) {
            throw new BizException("邮箱已注册");
        }

        // 注册创建账号
        User user = new User();
        user.setEmail(req.email().trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNickname(
                req.nickname() != null ? SanitizeUtils.escapeHtml(req.nickname().trim()) : "");
        save(user);

        // 注册时初始化默认数据：日常账本 + 现金账户（与用户数据同事务）
        AccountBook book = new AccountBook();
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

        // 根据注册数据生成 token, refreshToken
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        log.info("用户注册成功 userId={} email={}", user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.password() == null) {
            throw new BizException(401, "邮箱或密码错误");
        }
        String rawPassword = rsaUtils.decrypt(req.password());
        User user = lambdaQuery().eq(User::getEmail, req.email().trim()).one();
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BizException(401, "邮箱或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        log.info("用户登录成功 userId={} email={}", user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
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
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new BizException("邮箱格式不正确");
        }

        User user = lambdaQuery().eq(User::getEmail, email.trim()).one();
        if (user == null) {
            throw new BizException("该邮箱未注册");
        }

        // 防频繁请求：验证码 5 分钟内不重发
        if (user.getResetCodeExpiresAt() != null && user.getResetCodeExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BizException("验证码已发送，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
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

        User user = lambdaQuery().eq(User::getEmail, email.trim()).one();
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
        if (rawPassword == null || rawPassword.length() < 6 || rawPassword.length() > 32) {
            throw new BizException("密码长度必须在6-32位之间");
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        updateById(user);
        log.info("密码重置成功 email={}", email);
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
        log.info("token 刷新成功 userId={}", userId);
        return new LoginResponse(newToken, newRefreshToken, userInfo);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}
