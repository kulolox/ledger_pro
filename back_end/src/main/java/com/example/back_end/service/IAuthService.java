package com.example.back_end.service;

import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.LoginResponse;

public interface IAuthService {
    LoginResponse register(RegisterRequest req);

    LoginResponse login(LoginRequest req);

    void logout(String accessToken, String refreshToken);

    void sendResetCode(String email);

    void resetPassword(String email, String code, String newPassword);

    LoginResponse refresh(String refreshToken);
}
