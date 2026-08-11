package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.service.IAccountBookService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
        AccountBook book = new AccountBook();
        book.setId(generateBookId());
        book.setName(SanitizeUtils.escapeHtml(req.name().trim()));
        book.setOwnerId(AuthUtils.getCurrentUserId());
        if (req.coverIcon() != null) book.setCoverIcon(SanitizeUtils.escapeHtml(req.coverIcon()));
        if (req.type() != null) book.setType(req.type());
        save(book);
        log.info("新增账本 userId={} bookId={} name={}", AuthUtils.getCurrentUserId(), book.getId(), book.getName());
        return AccountBookResponse.from(book);
    }

    // 账本 id 用随机 8-12 位数字而非自增，避免通过连续 id 遍历他人账本
    private Long generateBookId() {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong(10000000L, 100000000000L);
        } while (getById(id) != null);
        return id;
    }
}
