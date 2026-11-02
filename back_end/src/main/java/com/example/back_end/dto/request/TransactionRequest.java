package com.example.back_end.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotNull Long book_id,
        @NotNull Integer type,
        @NotNull @Positive(message = "交易金额必须大于0") @Max(value = 2000000000L, message = "交易金额超出上限(单笔最大 2000 万元)")
                Integer amount,
        @NotNull Long account_id,
        Long to_account_id,
        @NotNull Long category_id,
        @Size(max = 500) String remark,
        @NotBlank String trans_time) {}
