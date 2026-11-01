package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.service.IAccountBookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account-books")
public class AccountBookController {
    private final IAccountBookService accountBookService;

    public AccountBookController(IAccountBookService accountBookService) {
        this.accountBookService = accountBookService;
    }

    @GetMapping
    public Result<List<AccountBookResponse>> list() {
        return Result.ok(accountBookService.listByOwner());
    }

    @PostMapping
    public Result<AccountBookResponse> create(@Valid @RequestBody CreateAccountBookRequest req) {
        return Result.ok("创建成功", accountBookService.create(req));
    }

    @PutMapping("/{id}")
    public Result<AccountBookResponse> update(@PathVariable Long id, @Valid @RequestBody CreateAccountBookRequest req) {
        return Result.ok("修改成功", accountBookService.update(id, req));
    }
}
