package com.example.back_end.controller;

import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionListResponse;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.service.ITransactionService;
import com.example.back_end.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Result<TransactionResponse> create(@Valid @RequestBody TransactionRequest req) {
        return Result.ok("记账成功", transactionService.create(req));
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> createBatch(@Valid @RequestBody BatchCreateRequest req) {
        return Result.ok("批量记账成功", transactionService.createBatch(req));
    }

    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDelete(@Valid @RequestBody BatchDeleteRequest req) {
        return Result.ok(Map.of("count", transactionService.batchDelete(req)));
    }

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam Long book_id, @RequestParam String month) {
        TransactionListResponse result = transactionService.list(book_id, month);
        return Result.ok(Map.of("list", result.list(), "total", result.total()));
    }

    @GetMapping("/export")
    public void exportTransactions(
            @RequestParam Long book_id,
            @RequestParam String month,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) {
        transactionService.exportTransactions(book_id, month, format, response);
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importTransactions(
            @RequestParam Long book_id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "csv") String format) {
        return Result.ok("导入完成", transactionService.importTransactions(book_id, file, format));
    }
}
