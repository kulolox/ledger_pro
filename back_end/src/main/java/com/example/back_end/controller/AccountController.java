package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.service.IAccountService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Result<List<AccountResponse>> list(@RequestParam Long book_id) {
        return Result.ok(accountService.listByBookId(book_id));
    }
}
