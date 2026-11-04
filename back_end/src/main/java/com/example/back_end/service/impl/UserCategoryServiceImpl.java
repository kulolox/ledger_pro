package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Category;
import com.example.back_end.entity.Transaction;
import com.example.back_end.entity.UserCategory;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.CategoryMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.mapper.UserCategoryMapper;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserCategoryServiceImpl extends ServiceImpl<UserCategoryMapper, UserCategory>
        implements IUserCategoryService {

    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;
    private final AccountBookMapper accountBookMapper;
    private final CacheManager cacheManager;

    public UserCategoryServiceImpl(
            CategoryMapper categoryMapper,
            TransactionMapper transactionMapper,
            AccountBookMapper accountBookMapper,
            CacheManager cacheManager) {
        this.categoryMapper = categoryMapper;
        this.transactionMapper = transactionMapper;
        this.accountBookMapper = accountBookMapper;
        this.cacheManager = cacheManager;
    }

    public List<CategoryResponse> listAll() {
        Long userId = AuthUtils.getCurrentUserId();
        List<UserCategory> list = lambdaQuery()
                .eq(UserCategory::getUserId, userId)
                .orderByAsc(UserCategory::getSortOrder)
                .list();

        // 用户首次访问分类时懒初始化：仅对当前 userId 细粒度同步，防止全局串行阻塞
        if (list.isEmpty()) {
            synchronized (("init-user-cat-" + userId).intern()) {
                list = lambdaQuery()
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
            }
        }

        return list.stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new BizException("分类名称不能为空");
        }
        if (req.type() == null || (req.type() != 0 && req.type() != 1)) {
            throw new BizException("无效的分类类型");
        }
        if (req.icon() == null || req.icon().isBlank()) {
            throw new BizException("图标不能为空");
        }
        if (req.color() == null || req.color().isBlank()) {
            throw new BizException("颜色不能为空");
        }

        Long userId = AuthUtils.getCurrentUserId();
        String cleanName = SanitizeUtils.escapeHtml(req.name().trim());
        String cleanIcon = SanitizeUtils.escapeHtml(req.icon());
        String cleanColor = SanitizeUtils.escapeHtml(req.color());

        if (lambdaQuery()
                        .eq(UserCategory::getUserId, userId)
                        .eq(UserCategory::getType, req.type())
                        .eq(UserCategory::getName, cleanName)
                        .count()
                > 0) {
            throw new BizException("同类型下已存在同名分类");
        }

        UserCategory uc = new UserCategory();
        uc.setUserId(userId);
        uc.setName(cleanName);
        uc.setType(req.type());
        uc.setIcon(cleanIcon);
        uc.setColor(cleanColor);
        uc.setParentId(req.parent_id() != null ? req.parent_id() : 0L);
        uc.setIsSystem(0);
        uc.setVisible(1);
        uc.setSortOrder(nextSortOrder(userId, req.type()));
        save(uc);
        log.info("创建自定义分类 userId={} name={}", userId, cleanName);
        return CategoryResponse.from(uc);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = getById(id);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BizException("无权操作该分类");
        }
        if (uc.getIsSystem() == 1) {
            if (req.name() != null || req.icon() != null || req.color() != null) {
                throw new BizException("系统分类只允许修改显示状态和排序");
            }
        } else {
            if (req.name() != null) {
                if (req.name().isBlank()) {
                    throw new BizException("分类名称不能为空");
                }
                String cleanName = SanitizeUtils.escapeHtml(req.name().trim());
                if (!cleanName.equals(uc.getName())
                        && lambdaQuery()
                                        .eq(UserCategory::getUserId, userId)
                                        .eq(UserCategory::getType, uc.getType())
                                        .eq(UserCategory::getName, cleanName)
                                        .ne(UserCategory::getId, id)
                                        .count()
                                > 0) {
                    throw new BizException("同类型下已存在同名分类");
                }
                uc.setName(cleanName);
            }
            if (req.icon() != null) uc.setIcon(SanitizeUtils.escapeHtml(req.icon()));
            if (req.color() != null) uc.setColor(SanitizeUtils.escapeHtml(req.color()));
        }
        if (req.visible() != null) uc.setVisible(req.visible());
        if (req.sort_order() != null) uc.setSortOrder(req.sort_order());
        updateById(uc);
        log.info("更新分类 userId={} categoryId={}", userId, id);
        return CategoryResponse.from(uc);
    }

    @Override
    public long countTransactions(Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = getById(id);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BizException("无权操作该分类");
        }
        List<Long> bookIds = getOwnedBookIds(userId);
        if (bookIds.isEmpty()) {
            return 0L;
        }
        return transactionMapper.selectCount(new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getCategoryId, id)
                .in(Transaction::getBookId, bookIds));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        delete(id, null, false);
    }

    @Override
    @Transactional
    public void delete(Long id, Long targetCategoryId, Boolean deleteTransactions) {
        Long userId = AuthUtils.getCurrentUserId();
        UserCategory uc = getById(id);
        if (uc == null || !uc.getUserId().equals(userId)) {
            throw new BizException("无权操作该分类");
        }
        if (uc.getIsSystem() == 1) {
            throw new BizException("系统分类不可删除");
        }

        List<Long> bookIds = getOwnedBookIds(userId);
        long txCount = bookIds.isEmpty()
                ? 0L
                : transactionMapper.selectCount(new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getCategoryId, id)
                        .in(Transaction::getBookId, bookIds));

        if (txCount > 0) {
            if (Boolean.TRUE.equals(deleteTransactions)) {
                transactionMapper.delete(new LambdaQueryWrapper<Transaction>()
                        .eq(Transaction::getCategoryId, id)
                        .in(Transaction::getBookId, bookIds));
                evictAllTransactionCache();
                log.info("删除分类连同账单 userId={} categoryId={} txCount={}", userId, id, txCount);
            } else if (targetCategoryId != null) {
                if (targetCategoryId.equals(id)) {
                    throw new BizException("目标归档分类不能为当前分类");
                }
                UserCategory targetUc = getById(targetCategoryId);
                if (targetUc == null || !targetUc.getUserId().equals(userId)) {
                    // 兼容前端传入的系统预设模板分类 ID (source_id) 或通过 id 双向检索
                    targetUc = lambdaQuery()
                            .eq(UserCategory::getUserId, userId)
                            .and(w -> w.eq(UserCategory::getSourceId, targetCategoryId)
                                    .or()
                                    .eq(UserCategory::getId, targetCategoryId))
                            .one();
                }
                if (targetUc == null) {
                    throw new BizException("目标归档分类不存在或无权操作");
                }
                if (targetUc.getId().equals(id)) {
                    throw new BizException("目标归档分类不能为当前分类");
                }
                if (!targetUc.getType().equals(uc.getType())) {
                    throw new BizException("目标归档分类与当前分类类型不一致");
                }

                Long finalTargetCatId = targetUc.getId();
                Transaction updateEntity = new Transaction();
                updateEntity.setCategoryId(finalTargetCatId);

                transactionMapper.update(
                        updateEntity,
                        new LambdaUpdateWrapper<Transaction>()
                                .eq(Transaction::getCategoryId, id)
                                .in(Transaction::getBookId, bookIds));
                evictAllTransactionCache();
                log.info(
                        "删除分类并迁移账单 userId={} categoryId={} targetCategoryId={} txCount={}",
                        userId,
                        id,
                        finalTargetCatId,
                        txCount);
            } else {
                throw new BizException("该分类下有关联账单，请选择归档分类或连同账单一起删除");
            }
        }

        removeById(id);
        log.info("删除分类 userId={} categoryId={}", userId, id);
    }

    @Transactional
    public void reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Long userId = AuthUtils.getCurrentUserId();
        for (int i = 0; i < ids.size(); i++) {
            lambdaUpdate()
                    .eq(UserCategory::getId, ids.get(i))
                    .eq(UserCategory::getUserId, userId)
                    .set(UserCategory::getSortOrder, i + 1)
                    .update();
        }
    }

    private List<Long> getOwnedBookIds(Long userId) {
        return accountBookMapper
                .selectList(new LambdaQueryWrapper<AccountBook>().eq(AccountBook::getOwnerId, userId))
                .stream()
                .map(AccountBook::getId)
                .toList();
    }

    private void evictAllTransactionCache() {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache("transactions");
            if (cache != null) {
                cache.clear();
            }
        }
    }

    // 从 category 模板表复制系统分类到该用户的 user_category，source 数据由 DataInitializer 启动时种入
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
            uc.setSortOrder(t.getSortOrder());
            uc.setSourceId(t.getId());
            list.add(uc);
        }
        saveBatch(list);
    }

    // 获取分类排序下一个序号
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
