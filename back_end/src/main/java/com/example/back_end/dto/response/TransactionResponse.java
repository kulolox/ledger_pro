package com.example.back_end.dto.response;

import com.example.back_end.entity.Transaction;
import java.io.Serializable;

public record TransactionResponse(
        Long id,
        Long book_id,
        Integer type,
        Integer amount,
        Long account_id,
        Long to_account_id,
        Long category_id,
        String remark,
        String trans_time,
        String created_at)
        implements Serializable {
    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getBookId(),
                tx.getType(),
                tx.getAmount(),
                tx.getAccountId(),
                tx.getToAccountId(),
                tx.getCategoryId(),
                tx.getRemark(),
                tx.getTransTime().toString(),
                tx.getCreatedAt().toString());
    }
}
