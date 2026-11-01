package com.example.back_end.dto.response;

import com.example.back_end.entity.User;
import java.time.format.DateTimeFormatter;

public record UserInfoResponse(Long id, String nickname, String avatar, String email, String phone, String createdAt) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public UserInfoResponse(Long id, String nickname, String avatar, String email, String phone) {
        this(id, nickname, avatar, email, phone, null);
    }

    public static UserInfoResponse from(User user) {
        String createdAtStr = user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : "";
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone(), createdAtStr);
    }
}
