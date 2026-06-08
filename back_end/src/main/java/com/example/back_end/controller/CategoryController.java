package com.example.back_end.controller;

import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.service.CategoryService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryResponse>> list(@RequestParam Long book_id) {
        return Result.ok(categoryService.list(book_id));
    }
}
