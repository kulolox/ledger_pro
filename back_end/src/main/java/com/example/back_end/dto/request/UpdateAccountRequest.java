package com.example.back_end.dto.request;

public record UpdateAccountRequest(
        String name,
        Integer type,
        String icon,
        String currency,
        Integer initial_balance,
        Integer is_include_total,
        Integer sort_order) {}
