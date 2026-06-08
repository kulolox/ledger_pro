package com.example.back_end.dto.response;

public record LoginResponse(String token, UserInfoResponse userInfo) {}
