package com.example.back_end.controller;

import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.service.AccountService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Result<List<AccountResponse>> list(@RequestParam Long book_id) {
        return Result.ok(accountService.list(book_id));
    }
}
