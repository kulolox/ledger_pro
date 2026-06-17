package com.example.back_end.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchCreateRequest(@NotEmpty @Valid List<TransactionRequest> items) {}
