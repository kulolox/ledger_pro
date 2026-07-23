package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.CreateAccountRequest;
import com.example.back_end.dto.request.UpdateAccountRequest;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import java.util.List;

public interface IAccountService extends IService<Account> {
    List<AccountResponse> listByBookId(Long bookId);

    AccountResponse create(CreateAccountRequest req);

    AccountResponse update(Long id, UpdateAccountRequest req);

    void delete(Long id, Long bookId);
}
