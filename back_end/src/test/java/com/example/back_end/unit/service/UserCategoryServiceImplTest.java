package com.example.back_end.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.entity.Category;
import com.example.back_end.entity.UserCategory;
import com.example.back_end.mapper.CategoryMapper;
import com.example.back_end.mapper.UserCategoryMapper;
import com.example.back_end.service.impl.UserCategoryServiceImpl;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Captor
    ArgumentCaptor<UserCategory> categoryCaptor;

    UserCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserCategoryServiceImpl(categoryMapper);
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

        when(userCategoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(seeded));

        Category template = new Category();
        template.setId(1L);
        template.setName("餐饮");
        template.setType(0);
        template.setIcon("food");
        template.setColor("#ff0000");
        template.setParentId(0L);
        template.setSortOrder(1);

        when(categoryMapper.selectList(null)).thenReturn(List.of(template));

        List<CategoryResponse> list = spyService.listAll();
        assertNotNull(list);
        assertEquals(1, list.size());
        verify(categoryMapper).selectList(null);
        verify(spyService).saveBatch(anyList());
    }

    @Test
    @DisplayName("create: 名称为空抛出异常")
    void create_blankName_throwsException() {
        CategoryRequest req = new CategoryRequest("  ", 0, "icon", "color", 0L, 1, 1);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("分类名称不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("create: 类型无效抛出异常")
    void create_invalidType_throwsException() {
        CategoryRequest req = new CategoryRequest("娱乐", 99, "icon", "color", 0L, 1, 1);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("分类类型不能为空且必须为0或1", ex.getMessage());
    }

    @Test
    @DisplayName("create: 同类型同名分类抛出异常")
    void create_duplicateName_throwsException() {
        when(userCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CategoryRequest req = new CategoryRequest("餐饮", 0, "icon", "color", 0L, 1, 1);
        BizException ex = assertThrows(BizException.class, () -> service.create(req));
        assertEquals("同类型下已存在同名分类", ex.getMessage());
    }

    @Test
    @DisplayName("create: 成功创建并正确转义")
    void create_success() {
        when(userCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userCategoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // nextSortOrder = 1
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
        assertEquals("系统分类不可编辑", ex.getMessage());
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
    @DisplayName("delete: 用户自定义分类可成功删除")
    void delete_customCategory_success() {
        UserCategory customCat = new UserCategory();
        customCat.setId(22L);
        customCat.setUserId(1L);
        customCat.setIsSystem(0);
        when(userCategoryMapper.selectById(22L)).thenReturn(customCat);

        service.delete(22L);
        verify(userCategoryMapper).deleteById(22L);
    }
}
