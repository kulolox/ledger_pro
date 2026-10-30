package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.service.IAccountBookService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
        String trimmed = req.name().trim();
        if (trimmed.length() > 50) {
            throw new BizException("账本名称长度不能超过50个字符");
        }
        AccountBook book = new AccountBook();
        book.setName(SanitizeUtils.escapeHtml(trimmed));
        book.setOwnerId(AuthUtils.getCurrentUserId());
        if (req.coverIcon() != null) book.setCoverIcon(SanitizeUtils.escapeHtml(req.coverIcon()));
        if (req.type() != null) book.setType(req.type());
        save(book);
        log.info("新增账本 userId={} bookId={} name={}", AuthUtils.getCurrentUserId(), book.getId(), book.getName());
        return AccountBookResponse.from(book);
    }
}
