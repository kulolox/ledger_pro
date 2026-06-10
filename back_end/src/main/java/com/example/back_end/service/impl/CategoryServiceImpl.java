package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.Category;
import com.example.back_end.mapper.CategoryMapper;
import com.example.back_end.service.ICategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    public List<CategoryResponse> listByBookId(Long bookId) {
        return lambdaQuery()
                .and(w -> w.isNull(Category::getBookId).or().eq(Category::getBookId, bookId))
                .list()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
