package com.example.back_end.dto.request;

import java.util.List;

public record BatchCreateRequest(List<TransactionRequest> items) {}
