package com.example.back_end.controller;

import com.example.back_end.dto.request.CreateBudgetRequest;
import com.example.back_end.dto.request.UpdateBudgetRequest;
import com.example.back_end.dto.response.BudgetResponse;
import com.example.back_end.service.IBudgetService;
import com.example.back_end.utils.Result;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final IBudgetService budgetService;

    public BudgetController(IBudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public Result<List<BudgetResponse>> list(@RequestParam Long book_id, @RequestParam String month) {
        return Result.ok(budgetService.listByBookId(book_id, month));
    }

    @PostMapping
    public Result<BudgetResponse> create(@Valid @RequestBody CreateBudgetRequest req) {
        return Result.ok("创建成功", budgetService.create(req));
    }

    @PutMapping("/{id}")
    public Result<BudgetResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateBudgetRequest req) {
        return Result.ok("修改成功", budgetService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, String>> delete(@PathVariable Long id, @RequestParam Long book_id) {
        budgetService.delete(id, book_id);
        return Result.ok("已删除", Map.of());
    }
}
