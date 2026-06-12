package com.example.back_end.controller;

import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.User;
import com.example.back_end.exception.BizException;
import com.example.back_end.service.IUserService;
import com.example.back_end.service.impl.MailService;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.Result;
import com.example.back_end.utils.RsaUtils;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final IUserService userService;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;
    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String RESET_CODE_PREFIX = "reset:code:";
    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthController(
            IUserService userService,
            JwtUtils jwtUtils,
            RsaUtils rsaUtils,
            StringRedisTemplate stringRedisTemplate,
            MailService mailService,
            BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.rsaUtils = rsaUtils;
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/public-key")
    public Result<Map<String, String>> publicKey() {
        return Result.ok(Map.of("publicKey", rsaUtils.getPublicKeyBase64()));
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest req) {
        String rawPassword = rsaUtils.decrypt(req.password());
        RegisterRequest decryptedReq = new RegisterRequest(req.email(), rawPassword, req.nickname());
        return Result.ok("注册成功", userService.register(decryptedReq));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        String rawPassword = rsaUtils.decrypt(req.password());
        return Result.ok("登录成功", userService.login(req.email(), rawPassword));
    }

    @PostMapping("/logout")
    public Result<Map<String, String>> logout(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refresh_token");
        if (refreshToken != null && jwtUtils.validateRefreshToken(refreshToken)) {
            Date exp = jwtUtils.getExpiration(refreshToken);
            long ttl = Duration.between(Instant.now(), exp.toInstant()).toSeconds();
            if (ttl > 0) {
                stringRedisTemplate
                        .opsForValue()
                        .set(TOKEN_BLACKLIST_PREFIX + refreshToken, "1", Duration.ofSeconds(ttl));
            }
        }
        return Result.ok("已退出登录", Map.of());
    }

    @PostMapping("/send-reset-code")
    public Result<Map<String, String>> sendResetCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new BizException("邮箱格式不正确");
        }

        User user = userService.lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            throw new BizException("该邮箱未注册");
        }

        String redisKey = RESET_CODE_PREFIX + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            throw new BizException("验证码已发送，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        mailService.sendResetCode(email, code);
        stringRedisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(5));

        return Result.ok("验证码已发送", Map.of());
    }

    @PostMapping("/reset-password")
    public Result<Map<String, String>> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("new_password");

        if (email == null || code == null || newPassword == null) {
            throw new BizException("参数不完整");
        }

        String redisKey = RESET_CODE_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new BizException("验证码错误或已过期");
        }

        User user = userService.lambdaQuery().eq(User::getEmail, email).one();
        if (user == null) {
            throw new BizException("用户不存在");
        }

        String rawPassword = rsaUtils.decrypt(newPassword);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userService.updateById(user);

        stringRedisTemplate.delete(redisKey);

        return Result.ok("密码重置成功", Map.of());
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refresh_token");
        if (refreshToken == null || !jwtUtils.validateRefreshToken(refreshToken)) {
            throw new BizException(401, "refresh_token无效或已过期，请重新登录");
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + refreshToken))) {
            throw new BizException(401, "refresh_token已被注销，请重新登录");
        }
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String email = jwtUtils.getEmailFromToken(refreshToken);
        var user = userService.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(401, "用户不存在");
        }
        String newToken = jwtUtils.generateToken(userId, email);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, email);
        var userInfo = new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
        return Result.ok(new LoginResponse(newToken, newRefreshToken, userInfo));
    }
}
