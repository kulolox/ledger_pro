package com.example.back_end.service;

import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> list(Long bookId) {
        return categoryRepository.findByBookIdOrSystem(bookId)
                .stream().map(CategoryResponse::from).toList();
    }
}
