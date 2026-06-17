package com.example.back_end.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 50) String nickname,
        @Size(max = 500) String avatar,
        @Size(max = 20) String phone) {}
