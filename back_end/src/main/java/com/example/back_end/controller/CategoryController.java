package com.example.back_end.controller;

import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.service.ICategoryService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryResponse>> list(@RequestParam Long book_id) {
        return Result.ok(categoryService.listByBookId(book_id));
    }
}
