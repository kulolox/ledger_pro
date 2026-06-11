package com.example.back_end.controller;

import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.exception.BizException;
import com.example.back_end.service.IUserService;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.Result;
import com.example.back_end.utils.RsaUtils;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final IUserService userService;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;

    public AuthController(IUserService userService, JwtUtils jwtUtils, RsaUtils rsaUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.rsaUtils = rsaUtils;
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

    @PostMapping("/send-reset-code")
    public Result<Map<String, String>> sendResetCode(@RequestBody Map<String, String> params) {
        return Result.ok("验证码已发送", Map.of());
    }

    @PostMapping("/reset-password")
    public Result<Map<String, String>> resetPassword(@RequestBody Map<String, String> params) {
        return Result.ok("密码重置成功", Map.of());
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refresh_token");
        if (refreshToken == null || !jwtUtils.validateRefreshToken(refreshToken)) {
            throw new BizException(401, "refresh_token无效或已过期，请重新登录");
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
