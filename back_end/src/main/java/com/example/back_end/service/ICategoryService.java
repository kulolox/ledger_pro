package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.Category;
import java.util.List;

public interface ICategoryService extends IService<Category> {
    List<CategoryResponse> listByBookId(Long bookId);
}
