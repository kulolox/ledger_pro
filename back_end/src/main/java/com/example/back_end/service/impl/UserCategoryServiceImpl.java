package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.Category;
import com.example.back_end.entity.UserCategory;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.CategoryMapper;
import com.example.back_end.mapper.UserCategoryMapper;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCategoryServiceImpl extends ServiceImpl<UserCategoryMapper, UserCategory> implements IUserCategoryService {

    private final CategoryMapper categoryMapper;

    public UserCategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public synchronized List<CategoryResponse> listAll() {
        Long userId = AuthUtils.getCurrentUserId();
        List<UserCategory> list = lambdaQuery()
                .eq(UserCategory::getUserId, userId)
                .orderByAsc(UserCategory::getSortOrder)
                .list();

        if (list.isEmpty()) {
            initForUser(userId);
            list = lambdaQuery()
                    .eq(UserCategory::getUserId, userId)
                    .orderByAsc(UserCategory::getSortOrder)
                    .list();
        }

        return list.stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = new UserCategory();
        uc.setUserId(userId);
        uc.setName(SanitizeUtils.escapeHtml(req.name()));
        uc.setType(req.type());
        uc.setIcon(req.icon() != null ? SanitizeUtils.escapeHtml(req.icon()) : "");
        uc.setColor(req.color() != null ? SanitizeUtils.escapeHtml(req.color()) : "");
        uc.setParentId(req.parent_id() != null ? req.parent_id() : 0L);
        uc.setIsSystem(0);
        uc.setSourceId(null);
        uc.setSortOrder(nextSortOrder(userId, req.type()));
        save(uc);
        return CategoryResponse.from(uc);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = getById(id);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BizException("无权操作该分类");
        }

        boolean isSystem = uc.getIsSystem() == 1;
        boolean hasIdentityChange = req.name() != null || req.icon() != null || req.color() != null;
        if (isSystem && hasIdentityChange) {
            throw new BizException("系统分类不可编辑");
        }
        if (!isSystem && hasIdentityChange) {
            if (req.name() != null) uc.setName(SanitizeUtils.escapeHtml(req.name()));
            if (req.icon() != null) uc.setIcon(SanitizeUtils.escapeHtml(req.icon()));
            if (req.color() != null) uc.setColor(SanitizeUtils.escapeHtml(req.color()));
        }
        if (req.visible() != null) uc.setVisible(req.visible());
        if (req.sort_order() != null) uc.setSortOrder(req.sort_order());
        uc.setUpdatedAt(LocalDateTime.now());
        updateById(uc);
        return CategoryResponse.from(uc);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = getById(id);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BizException("无权操作该分类");
        }
        if (uc.getIsSystem() == 1) {
            throw new BizException("系统分类不可删除");
        }
        removeById(id);
    }

    @Transactional
    public void reorder(List<Long> ids) {
        Long userId = AuthUtils.getCurrentUserId();
        for (int i = 0; i < ids.size(); i++) {
            lambdaUpdate()
                    .eq(UserCategory::getId, ids.get(i))
                    .eq(UserCategory::getUserId, userId)
                    .set(UserCategory::getSortOrder, i + 1)
                    .update();
        }
    }

    private void initForUser(Long userId) {
        List<Category> templates = categoryMapper.selectList(null);
        List<UserCategory> list = new ArrayList<>();
        for (Category t : templates) {
            UserCategory uc = new UserCategory();
            uc.setUserId(userId);
            uc.setName(t.getName());
            uc.setType(t.getType());
            uc.setIcon(t.getIcon());
            uc.setColor(t.getColor());
            uc.setParentId(t.getParentId());
            uc.setIsSystem(1);
            uc.setSourceId(t.getId());
            uc.setSortOrder(t.getSortOrder());
            list.add(uc);
        }
        saveBatch(list);
    }

    private int nextSortOrder(Long userId, Integer type) {
        UserCategory last = lambdaQuery()
                .eq(UserCategory::getUserId, userId)
                .eq(UserCategory::getType, type)
                .orderByDesc(UserCategory::getSortOrder)
                .last("LIMIT 1")
                .one();
        return last != null ? last.getSortOrder() + 1 : 1;
    }
}
