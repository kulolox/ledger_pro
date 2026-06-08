package com.example.back_end.controller;

import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.service.AccountBookService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-books")
public class AccountBookController {
    private final AccountBookService accountBookService;

    public AccountBookController(AccountBookService accountBookService) {
        this.accountBookService = accountBookService;
    }

    @GetMapping
    public Result<List<AccountBookResponse>> list() {
        return Result.ok(accountBookService.list());
    }

    @PostMapping
    public Result<AccountBookResponse> create(@RequestBody CreateAccountBookRequest req) {
        return Result.ok("创建成功", accountBookService.create(req));
    }
}
