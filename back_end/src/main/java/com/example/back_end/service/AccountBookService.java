package com.example.back_end.service;

import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.exception.BizException;
import com.example.back_end.repository.AccountBookRepository;
import com.example.back_end.utils.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountBookService {
    private final AccountBookRepository accountBookRepository;

    public AccountBookService(AccountBookRepository accountBookRepository) {
        this.accountBookRepository = accountBookRepository;
    }

    public List<AccountBookResponse> list() {
        Long userId = AuthUtils.getCurrentUserId();
        return accountBookRepository.findByOwnerIdAndDeleted(userId, 0)
                .stream().map(AccountBookResponse::from).toList();
    }

    public AccountBookResponse create(CreateAccountBookRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new BizException("账本名称不能为空");
        }
        AccountBook book = new AccountBook();
        book.setName(req.name().trim());
        book.setOwnerId(AuthUtils.getCurrentUserId());
        if (req.coverIcon() != null) book.setCoverIcon(req.coverIcon());
        if (req.type() != null) book.setType(req.type());
        book = accountBookRepository.save(book);
        return AccountBookResponse.from(book);
    }
}
