package com.example.back_end.dto.response;

import java.io.Serializable;
import java.util.List;

public record TransactionListResponse(List<TransactionResponse> list, int total) implements Serializable {}
