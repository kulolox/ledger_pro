package com.example.back_end.dto.response;

import com.example.back_end.entity.AccountBook;

public record AccountBookResponse(Long id, String name, String coverIcon, Integer type) {
    public static AccountBookResponse from(AccountBook b) {
        return new AccountBookResponse(b.getId(), b.getName(), b.getCoverIcon(), b.getType());
    }
}
