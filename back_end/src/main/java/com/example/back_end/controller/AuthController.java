package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.service.IAuthService;
import com.example.back_end.utils.RsaUtils;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final IAuthService authService;
    private final RsaUtils rsaUtils;

    public AuthController(IAuthService authService, RsaUtils rsaUtils) {
        this.authService = authService;
        this.rsaUtils = rsaUtils;
    }

    @GetMapping("/public-key")
    public Result<Map<String, String>> publicKey() {
        return Result.ok(Map.of("publicKey", rsaUtils.getPublicKeyBase64()));
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok("注册成功", authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok("登录成功", authService.login(req));
    }

    @PostMapping("/logout")
    public Result<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> params) {
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        authService.logout(accessToken, params.get("refresh_token"));
        return Result.ok("已退出登录", Map.of());
    }

    @PostMapping("/send-reset-code")
    public Result<Map<String, String>> sendResetCode(@RequestBody Map<String, String> params) {
        authService.sendResetCode(params.get("email"));
        return Result.ok("验证码已发送", Map.of());
    }

    @PostMapping("/reset-password")
    public Result<Map<String, String>> resetPassword(@RequestBody Map<String, String> params) {
        authService.resetPassword(
                params.get("email"), params.get("code"), params.get("new_password"));
        return Result.ok("密码重置成功", Map.of());
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> params) {
        return Result.ok(authService.refresh(params.get("refresh_token")));
    }
}
