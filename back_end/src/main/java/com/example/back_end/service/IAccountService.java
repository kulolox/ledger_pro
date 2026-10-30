package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import java.util.List;

public interface IAccountService extends IService<Account> {
    List<AccountResponse> listByBookId(Long bookId);
}
