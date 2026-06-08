package com.example.back_end.dto.request;

public record TransactionRequest(
    Long book_id,
    Integer type,
    Integer amount,
    Long account_id,
    Long to_account_id,
    Long category_id,
    String remark,
    String trans_time
) {}
