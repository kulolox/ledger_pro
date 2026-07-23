package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.CreateBudgetRequest;
import com.example.back_end.dto.request.UpdateBudgetRequest;
import com.example.back_end.dto.response.BudgetResponse;
import com.example.back_end.entity.Budget;
import java.util.List;

public interface IBudgetService extends IService<Budget> {
    List<BudgetResponse> listByBookId(Long bookId, String month);

    BudgetResponse create(CreateBudgetRequest req);

    BudgetResponse update(Long id, UpdateBudgetRequest req);

    void delete(Long id, Long bookId);
}
