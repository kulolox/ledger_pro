package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.service.IAccountBookService;
import com.example.back_end.utils.AuthUtils;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountBookServiceImpl extends ServiceImpl<AccountBookMapper, AccountBook> implements IAccountBookService {

    public List<AccountBookResponse> listByOwner() {
        Long userId = AuthUtils.getCurrentUserId();
        return lambdaQuery().eq(AccountBook::getOwnerId, userId).list().stream()
                .map(AccountBookResponse::from)
                .toList();
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
        save(book);
        return AccountBookResponse.from(book);
    }
}
