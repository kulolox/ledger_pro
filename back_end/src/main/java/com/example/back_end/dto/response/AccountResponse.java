package com.example.back_end.dto.response;

import com.example.back_end.entity.Account;

public record AccountResponse(Long id, String name, Integer type, String icon, String currency) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getType(), a.getIcon(), a.getCurrency());
    }
}
