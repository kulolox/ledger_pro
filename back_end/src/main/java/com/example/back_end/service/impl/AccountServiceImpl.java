package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CreateAccountRequest;
import com.example.back_end.dto.request.UpdateAccountRequest;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.service.IAccountService;
import com.example.back_end.utils.AuthUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {

    private final AccountBookMapper accountBookMapper;

    public AccountServiceImpl(AccountBookMapper accountBookMapper) {
        this.accountBookMapper = accountBookMapper;
    }

    private AccountBook validateBookOwnership(Long bookId) {
        var book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
        return book;
    }

    public List<AccountResponse> listByBookId(Long bookId) {
        validateBookOwnership(bookId);
        return lambdaQuery().eq(Account::getBookId, bookId).list().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest req) {
        validateBookOwnership(req.book_id());

        Account a = new Account();
        a.setBookId(req.book_id());
        a.setName(req.name());
        a.setType(req.type());
        a.setIcon(req.icon() != null ? req.icon() : "");
        a.setCurrency(req.currency() != null ? req.currency() : "CNY");
        a.setInitialBalance(req.initial_balance() != null ? req.initial_balance() : 0);
        a.setIsIncludeTotal(req.is_include_total() != null ? req.is_include_total() : 1);
        a.setSortOrder(req.sort_order() != null ? req.sort_order() : 0);
        save(a);
        return AccountResponse.from(a);
    }

    @Transactional
    public AccountResponse update(Long id, UpdateAccountRequest req) {
        Account a = getById(id);
        if (a == null) {
            throw new BizException("账户不存在");
        }
        // owner check via book
        validateBookOwnership(a.getBookId());

        if (req.name() != null) a.setName(req.name());
        if (req.type() != null) a.setType(req.type());
        if (req.icon() != null) a.setIcon(req.icon());
        if (req.currency() != null) a.setCurrency(req.currency());
        if (req.initial_balance() != null) a.setInitialBalance(req.initial_balance());
        if (req.is_include_total() != null) a.setIsIncludeTotal(req.is_include_total());
        if (req.sort_order() != null) a.setSortOrder(req.sort_order());
        a.setUpdatedAt(LocalDateTime.now());
        updateById(a);
        return AccountResponse.from(a);
    }

    @Transactional
    public void delete(Long id, Long bookId) {
        validateBookOwnership(bookId);

        Account a = getById(id);
        if (a == null || !a.getBookId().equals(bookId)) {
            throw new BizException("账户不存在");
        }
        a.setDeleted(1);
        a.setUpdatedAt(LocalDateTime.now());
        updateById(a);
    }
}
