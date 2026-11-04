package com.example.back_end.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 500) String password,
        @Size(max = 50) String nickname,
        @NotBlank(message = "验证码不能为空") String code) {

    public RegisterRequest(String email, String password, String nickname) {
        this(email, password, nickname, null);
    }
}
