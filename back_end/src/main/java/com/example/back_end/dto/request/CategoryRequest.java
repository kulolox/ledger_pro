package com.example.back_end.dto.request;

public record CategoryRequest(
        String name, Integer type, String icon, String color, Long parent_id, Integer sort_order, Integer visible) {}
