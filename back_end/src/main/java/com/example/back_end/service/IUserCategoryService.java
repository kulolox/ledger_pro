package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.UserCategory;
import java.util.List;

public interface IUserCategoryService extends IService<UserCategory> {
    List<CategoryResponse> listAll();

    CategoryResponse create(CategoryRequest req);

    CategoryResponse update(Long id, CategoryRequest req);

    void delete(Long id);

    void reorder(List<Long> ids);
}
