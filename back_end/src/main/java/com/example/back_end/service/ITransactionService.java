package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.Transaction;
import java.util.Map;

public interface ITransactionService extends IService<Transaction> {
    TransactionResponse create(TransactionRequest req);

    Map<String, Object> createBatch(BatchCreateRequest body);

    int batchDelete(BatchDeleteRequest body);

    Map<String, Object> list(Long bookId, String month);
}
