package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.Category;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.CategoryMapper;
import com.example.back_end.service.ICategoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    public List<CategoryResponse> listAll() {
        return lambdaQuery().orderByAsc(Category::getSortOrder).list().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse create(CategoryRequest req) {
        Category c = new Category();
        c.setName(req.name());
        c.setType(req.type());
        c.setIcon(req.icon() != null ? req.icon() : "");
        c.setColor(req.color() != null ? req.color() : "");
        c.setParentId(req.parent_id() != null ? req.parent_id() : 0L);
        c.setIsSystem(0);
        c.setSortOrder(nextSortOrder(req.type()));
        save(c);
        return CategoryResponse.from(c);
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = getById(id);
        if (c == null) throw new BizException("分类不存在");

        boolean isSystem = c.getIsSystem() == 1;
        boolean hasIdentityChange = req.name() != null || req.icon() != null || req.color() != null;
        if (isSystem && hasIdentityChange) {
            throw new BizException("系统分类不可编辑");
        }
        if (!isSystem) {
            if (req.name() != null) c.setName(req.name());
            if (req.icon() != null) c.setIcon(req.icon());
            if (req.color() != null) c.setColor(req.color());
        }
        if (req.sort_order() != null) c.setSortOrder(req.sort_order());
        if (req.visible() != null) c.setVisible(req.visible());
        c.setUpdatedAt(LocalDateTime.now());
        updateById(c);
        return CategoryResponse.from(c);
    }

    public void delete(Long id) {
        Category c = getById(id);
        if (c == null || c.getIsSystem() == 1) {
            throw new BizException("系统分类不可删除");
        }
        removeById(id);
    }

    public void reorder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            lambdaUpdate()
                    .eq(Category::getId, ids.get(i))
                    .set(Category::getSortOrder, i + 1)
                    .update();
        }
    }

    private int nextSortOrder(Integer type) {
        Category last = lambdaQuery()
                .eq(Category::getType, type)
                .orderByDesc(Category::getSortOrder)
                .last("LIMIT 1")
                .one();
        return last != null ? last.getSortOrder() + 1 : 1;
    }
}
