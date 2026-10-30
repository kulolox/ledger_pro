package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionListResponse;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.Transaction;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ITransactionService extends IService<Transaction> {
    TransactionResponse create(TransactionRequest req);

    TransactionResponse update(Long id, TransactionRequest req);

    Map<String, Object> createBatch(BatchCreateRequest body);

    int batchDelete(BatchDeleteRequest body);

    TransactionListResponse list(Long bookId, String month, Integer page, Integer size);

    TransactionListResponse search(
            Long bookId,
            String keyword,
            Integer type,
            Long categoryId,
            Integer amountMin,
            Integer amountMax,
            String dateFrom,
            String dateTo,
            Integer page,
            Integer size);

    void exportTransactions(Long bookId, String month, String year, String format, HttpServletResponse response);

    Map<String, Object> importTransactions(Long bookId, MultipartFile file, String format);

    void downloadTemplate(String format, HttpServletResponse response);
}
