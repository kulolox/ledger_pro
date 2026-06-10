package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.service.IAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {

    public List<AccountResponse> listByBookId(Long bookId) {
        return lambdaQuery().eq(Account::getBookId, bookId).list()
                .stream().map(AccountResponse::from).toList();
    }
}
