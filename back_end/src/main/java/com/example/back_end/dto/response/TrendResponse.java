package com.example.back_end.dto.response;

import java.util.List;

public record TrendResponse(List<String> months, List<Long> expense, List<Long> income) {}
