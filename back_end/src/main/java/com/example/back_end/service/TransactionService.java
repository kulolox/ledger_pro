package com.example.back_end.service;

import com.example.back_end.dto.request.BatchCreateRequest;
import com.example.back_end.dto.request.BatchDeleteRequest;
import com.example.back_end.dto.request.TransactionRequest;
import com.example.back_end.dto.response.TransactionResponse;
import com.example.back_end.entity.Transaction;
import com.example.back_end.exception.BizException;
import com.example.back_end.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse create(TransactionRequest req) {
        Transaction tx = toEntity(req);
        tx = transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public Map<String, Object> createBatch(BatchCreateRequest body) {
        if (body.items() == null || body.items().isEmpty()) {
            throw new BizException("items 不能为空");
        }
        List<Transaction> txs = new ArrayList<>();
        for (TransactionRequest params : body.items()) {
            txs.add(toEntity(params));
        }
        txs = transactionRepository.saveAll(txs);
        List<TransactionResponse> list = txs.stream().map(TransactionResponse::from).toList();
        return Map.of("list", list, "count", list.size());
    }

    @Transactional
    public int batchDelete(BatchDeleteRequest body) {
        if (body.ids() == null || body.ids().isEmpty()) {
            throw new BizException("ids 不能为空");
        }
        List<Transaction> txs = transactionRepository.findAllById(body.ids());
        for (Transaction tx : txs) {
            tx.setDeleted(1);
        }
        transactionRepository.saveAll(txs);
        return txs.size();
    }

    public Map<String, Object> list(Long bookId, String month) {
        LocalDateTime monthStart = LocalDateTime.parse(month + "-01T00:00:00");
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        List<Transaction> txs = transactionRepository.findByBookIdAndMonth(bookId, monthStart, monthEnd);
        List<TransactionResponse> list = txs.stream().map(TransactionResponse::from).toList();
        return Map.of("list", list, "total", list.size());
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
            tx.setTransTime(LocalDateTime.parse(req.trans_time().replace(" ", "T").substring(0, 19)));
        } else {
            tx.setTransTime(LocalDateTime.now());
        }
        return tx;
    }
}
