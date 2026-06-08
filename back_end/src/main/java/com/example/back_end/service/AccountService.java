package com.example.back_end.service;

import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountResponse> list(Long bookId) {
        return accountRepository.findByBookIdAndDeleted(bookId, 0)
                .stream().map(AccountResponse::from).toList();
    }
}
