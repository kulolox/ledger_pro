package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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
import com.example.back_end.service.impl.UserCategoryServiceImpl;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCategoryServiceImpl Unit Tests")
class UserCategoryServiceImplTest {

    @Mock
    UserCategoryMapper userCategoryMapper;

    @Mock
    CategoryMapper categoryMapper;

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    AccountBookMapper accountBookMapper;

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache cache;

    @Captor
    ArgumentCaptor<UserCategory> categoryCaptor;

    UserCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Transaction.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AccountBook.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), UserCategory.class);

        service = new UserCategoryServiceImpl(categoryMapper, transactionMapper, accountBookMapper, cacheManager);
        ReflectionTestUtils.setField(service, "baseMapper", userCategoryMapper);
        ReflectionTestUtils.setField(service, "entityClass", UserCategory.class);
        ReflectionTestUtils.setField(service, "mapperClass", UserCategoryMapper.class);

        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public String getName() {
                return "1";
            }

            @Override
            public Object getPrincipal() {
                return 1L;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean b) {}

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return List.of();
            }
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("listAll: 用户已有分类时直接返回")
    void listAll_existingCategories_returnsDirectly() {
        UserCategory cat = new UserCategory();
        cat.setId(10L);
        cat.setUserId(1L);
        cat.setName("餐饮");
        cat.setType(0);
        cat.setIsSystem(1);

        when(userCategoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cat));

        List<CategoryResponse> list = service.listAll();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("餐饮", list.get(0).name());
        verify(categoryMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("listAll: 用户首次访问懒初始化系统分类")
    void listAll_empty_initializesFromTemplates() {
        UserCategoryServiceImpl spyService = spy(service);
        doReturn(true).when(spyService).saveBatch(anyList());

        UserCategory seeded = new UserCategory();
        seeded.setId(101L);
        seeded.setUserId(1L);
        seeded.setName("餐饮");
        seeded.setType(0);
        seeded.setIsSystem(1);

        when(userCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(seeded));

        Category tmpl = new Category();
        tmpl.setId(1L);
        tmpl.setName("餐饮");
        tmpl.setType(0);
        tmpl.setIcon("🍔");
        tmpl.setColor("#ff6b35");
        tmpl.setParentId(0L);
        tmpl.setSortOrder(1);
        when(categoryMapper.selectList(null)).thenReturn(List.of(tmpl));

        List<CategoryResponse> list = spyService.listAll();
        assertNotNull(list);
        assertEquals(1, list.size());
        verify(categoryMapper, times(1)).selectList(null);
        verify(spyService, times(1)).saveBatch(anyList());
    }

    @Test
    @DisplayName("create: 分类名称为空抛出异常")
    void create_blankName_throwsException() {
        CategoryRequest req = new CategoryRequest("   ", 0, "icon", "color", 0L, null, null);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("分类名称不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("create: 无效的分类类型抛出异常")
    void create_invalidType_throwsException() {
        CategoryRequest req = new CategoryRequest("餐饮", 99, "icon", "color", 0L, null, null);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("无效的分类类型", ex.getMessage());
    }

    @Test
    @DisplayName("create: 同类型下存在同名分类抛出异常")
    void create_duplicateName_throwsException() {
        when(userCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CategoryRequest req = new CategoryRequest("餐饮", 0, "icon", "color", 0L, null, null);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("同类型下已存在同名分类", ex.getMessage());
    }

    @Test
    @DisplayName("create: 成功创建自定义分类并进行 XSS 清洗")
    void create_success() {
        when(userCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userCategoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userCategoryMapper.insert(any(UserCategory.class))).thenAnswer(inv -> {
            UserCategory c = inv.getArgument(0);
            c.setId(55L);
            return 1;
        });

        CategoryRequest req = new CategoryRequest("<b>自制美食</b>", 0, "icon", "color", 0L, null, 1);
        CategoryResponse res = service.create(req);

        assertNotNull(res);
        assertEquals(55L, res.id());
        assertTrue(res.name().contains("&lt;b&gt;"));
        assertEquals(1, res.sort_order());
    }

    @Test
    @DisplayName("update: 分类不存在或不归属当前用户抛出异常")
    void update_notOwner_throwsException() {
        when(userCategoryMapper.selectById(12L)).thenReturn(null);

        CategoryRequest req = new CategoryRequest("修改", 0, null, null, null, null, null);
        BizException ex = assertThrows(BizException.class, () -> service.update(12L, req));
        assertEquals("无权操作该分类", ex.getMessage());
    }

    @Test
    @DisplayName("update: 系统分类修改名称/图标/颜色被禁止")
    void update_systemCategory_cannotChangeIdentity() {
        UserCategory systemCat = new UserCategory();
        systemCat.setId(1L);
        systemCat.setUserId(1L);
        systemCat.setIsSystem(1);
        systemCat.setName("餐饮");
        when(userCategoryMapper.selectById(1L)).thenReturn(systemCat);

        CategoryRequest req = new CategoryRequest("新名称", null, null, null, null, null, null);
        BizException ex = assertThrows(BizException.class, () -> service.update(1L, req));
        assertEquals("系统分类只允许修改显示状态和排序", ex.getMessage());
    }

    @Test
    @DisplayName("update: 系统分类只允许修改可见性和排序")
    void update_systemCategory_allowVisibleAndSortOrder() {
        UserCategory systemCat = new UserCategory();
        systemCat.setId(1L);
        systemCat.setUserId(1L);
        systemCat.setIsSystem(1);
        systemCat.setVisible(1);
        when(userCategoryMapper.selectById(1L)).thenReturn(systemCat);

        CategoryRequest req = new CategoryRequest(null, null, null, null, null, 5, 0);
        CategoryResponse res = service.update(1L, req);

        assertNotNull(res);
        assertEquals(0, res.visible());
        assertEquals(5, res.sort_order());
        verify(userCategoryMapper).updateById(systemCat);
    }

    @Test
    @DisplayName("delete: 系统分类禁止删除")
    void delete_systemCategory_forbidden() {
        UserCategory systemCat = new UserCategory();
        systemCat.setId(1L);
        systemCat.setUserId(1L);
        systemCat.setIsSystem(1);
        when(userCategoryMapper.selectById(1L)).thenReturn(systemCat);

        BizException ex = assertThrows(BizException.class, () -> service.delete(1L));
        assertEquals("系统分类不可删除", ex.getMessage());
    }

    @Test
    @DisplayName("delete: 无关联账单的自定义分类可直接删除")
    void delete_customCategory_withoutTransactions_success() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        customCat.setIsSystem(0);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));
        when(transactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        service.delete(22L);
        verify(userCategoryMapper).deleteById(22L);
        verify(transactionMapper, never()).delete(any(LambdaQueryWrapper.class));
        verify(transactionMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("countTransactions: 正确统计分类下关联账单数量")
    void countTransactions_success() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        AccountBook book = new AccountBook();
        book.setId(100L);
        book.setOwnerId(1L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));
        when(transactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(15L);

        long count = service.countTransactions(22L);
        assertEquals(15L, count);
    }

    @Test
    @DisplayName("delete: 有账单但未选迁移或连同删除时抛出异常")
    void delete_withTransactions_withoutOption_throwsException() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        customCat.setIsSystem(0);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        AccountBook book = new AccountBook();
        book.setId(100L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));
        when(transactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        BizException ex = assertThrows(BizException.class, () -> service.delete(22L, null, false));
        assertEquals("该分类下有关联账单，请选择归档分类或连同账单一起删除", ex.getMessage());
    }

    @Test
    @DisplayName("delete: 有账单且选择迁移归档成功")
    void delete_withTransactions_migrate_success() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        customCat.setType(0);
        customCat.setIsSystem(0);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        UserCategory targetCat = new UserCategory();
        targetCat.setId(33L);
        targetCat.setUserId(1L);
        targetCat.setType(0);
        when(userCategoryMapper.selectById(33L)).thenReturn(targetCat);

        AccountBook book = new AccountBook();
        book.setId(100L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));
        when(transactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(cacheManager.getCache("transactions")).thenReturn(cache);

        service.delete(22L, 33L, false);

        verify(transactionMapper).update(any(Transaction.class), any(LambdaUpdateWrapper.class));
        verify(userCategoryMapper).deleteById(22L);
        verify(cache).clear();
    }

    @Test
    @DisplayName("delete: 有账单且选择连同账单一起删除成功")
    void delete_withTransactions_deleteTransactions_success() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        customCat.setType(0);
        customCat.setIsSystem(0);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        AccountBook book = new AccountBook();
        book.setId(100L);
        when(accountBookMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(book));
        when(transactionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(8L);
        when(cacheManager.getCache("transactions")).thenReturn(cache);

        service.delete(22L, null, true);

        verify(transactionMapper).delete(any(LambdaQueryWrapper.class));
        verify(userCategoryMapper).deleteById(22L);
        verify(cache).clear();
    }
}
