package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionListResponse;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.Transaction;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.service.ITransactionService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements ITransactionService {

    @CacheEvict(value = "transactions", allEntries = true)
    @Transactional
    public TransactionResponse create(TransactionRequest req) {
        Transaction tx = toEntity(req);
        save(tx);
        return TransactionResponse.from(tx);
    }

    @CacheEvict(value = "transactions", allEntries = true)
    @Transactional
    public Map<String, Object> createBatch(BatchCreateRequest body) {
        if (body.items() == null || body.items().isEmpty()) {
            throw new BizException("items 不能为空");
        }
        List<Transaction> txs = new ArrayList<>();
        for (TransactionRequest params : body.items()) {
            txs.add(toEntity(params));
        }
        saveBatch(txs);
        List<TransactionResponse> list =
                txs.stream().map(TransactionResponse::from).toList();
        return Map.of("list", list, "count", list.size());
    }

    @CacheEvict(value = "transactions", allEntries = true)
    @Transactional
    public int batchDelete(BatchDeleteRequest body) {
        if (body.ids() == null || body.ids().isEmpty()) {
            throw new BizException("ids 不能为空");
        }
        UpdateWrapper<Transaction> wrapper = new UpdateWrapper<>();
        wrapper.in("id", body.ids()).set("deleted", 1).set("updated_at", LocalDateTime.now());
        update(wrapper);
        return body.ids().size();
    }

    @Cacheable(value = "transactions", key = "#bookId + '::' + #month", unless = "#result == null")
    public TransactionListResponse list(Long bookId, String month) {
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .ge(Transaction::getTransTime, monthStart)
                .lt(Transaction::getTransTime, monthEnd)
                .orderByDesc(Transaction::getTransTime);

        List<Transaction> txs = list(wrapper);
        List<TransactionResponse> list =
                txs.stream().map(TransactionResponse::from).toList();
        return new TransactionListResponse(list, list.size());
    }

    private Transaction toEntity(TransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setBookId(req.book_id());
        tx.setType(req.type());
        tx.setAmount(req.amount());
        tx.setAccountId(req.account_id());
        tx.setToAccountId(req.to_account_id());
        tx.setCategoryId(req.category_id());
        tx.setRemark(req.remark() != null ? req.remark() : "");
        if (req.trans_time() != null && !req.trans_time().isBlank()) {
            tx.setTransTime(
                    LocalDateTime.parse(req.trans_time().replace(" ", "T").substring(0, 19)));
        } else {
            tx.setTransTime(LocalDateTime.now());
        }
        return tx;
    }
}
