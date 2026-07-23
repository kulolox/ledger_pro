package com.example.back_end.controller;

import com.example.back_end.dto.request.CreateAccountRequest;
import com.example.back_end.dto.request.UpdateAccountRequest;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.service.IAccountService;
import com.example.back_end.utils.Result;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

    @PostMapping
    public Result<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        return Result.ok("创建成功", accountService.create(req));
    }

    @PutMapping("/{id}")
    public Result<AccountResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest req) {
        return Result.ok("修改成功", accountService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, String>> delete(@PathVariable Long id, @RequestParam Long book_id) {
        accountService.delete(id, book_id);
        return Result.ok("已删除", Map.of());
    }
}
