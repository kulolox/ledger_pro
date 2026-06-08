package com.example.back_end.controller;

import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.service.TransactionService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Result<TransactionResponse> create(@RequestBody TransactionRequest req) {
        return Result.ok("记账成功", transactionService.create(req));
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> createBatch(@RequestBody BatchCreateRequest req) {
        return Result.ok("批量记账成功", transactionService.createBatch(req));
    }

    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDelete(@RequestBody BatchDeleteRequest req) {
        return Result.ok(Map.of("count", transactionService.batchDelete(req)));
    }

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam Long book_id, @RequestParam String month) {
        return Result.ok(transactionService.list(book_id, month));
    }
}
