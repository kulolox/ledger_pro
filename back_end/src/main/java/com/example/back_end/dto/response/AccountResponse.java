package com.example.back_end.dto.response;

import com.example.back_end.entity.Account;

public record AccountResponse(
        Long id,
        String name,
        Integer type,
        String icon,
        String currency,
        Integer initial_balance,
        Integer is_include_total,
        Integer sort_order,
        Integer deleted) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getIcon(),
                a.getCurrency(),
                a.getInitialBalance(),
                a.getIsIncludeTotal(),
                a.getSortOrder(),
                a.getDeleted());
    }
}
