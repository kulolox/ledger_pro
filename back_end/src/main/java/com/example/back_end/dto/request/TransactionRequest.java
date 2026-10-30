package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotNull Long book_id,
        @NotNull Integer type,
        @NotNull Integer amount,
        @NotNull Long account_id,
        Long to_account_id,
        @NotNull Long category_id,
        @Size(max = 500) String remark,
        @NotBlank String trans_time) {}
