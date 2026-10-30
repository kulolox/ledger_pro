package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.response.AccountResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.service.IAccountService;
import com.example.back_end.utils.AuthUtils;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {

    private final AccountBookMapper accountBookMapper;

    public AccountServiceImpl(AccountBookMapper accountBookMapper) {
        this.accountBookMapper = accountBookMapper;
    }

    // 账本归属校验：账户操作必须发生在当前用户拥有的账本下
    private void validateBookOwnership(Long bookId) {
        var book = accountBookMapper.selectById(bookId);
        if (book == null || !book.getOwnerId().equals(AuthUtils.getCurrentUserId())) {
            throw new BizException("无权访问该账本");
        }
    }

    public List<AccountResponse> listByBookId(Long bookId) {
        validateBookOwnership(bookId);
        return lambdaQuery().eq(Account::getBookId, bookId).orderByAsc(Account::getSortOrder).list().stream()
                .map(AccountResponse::from)
                .toList();
    }
}
