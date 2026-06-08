package com.example.back_end.controller;

import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.service.UserService;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest req) {
        return Result.ok("注册成功", userService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        return Result.ok("登录成功", userService.login(req.email(), req.password()));
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
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> params) {
        String token = jwtUtils.generateToken(1L, "");
        return Result.ok(Map.of("token", token));
    }
}
