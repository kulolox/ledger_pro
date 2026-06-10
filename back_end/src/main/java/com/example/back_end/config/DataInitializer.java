package com.example.back_end.config;

import com.example.back_end.entity.Category;
import com.example.back_end.service.ICategoryService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ICategoryService categoryService;

    public DataInitializer(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        if (categoryService.count() > 0) return;

        List<Category> categories = List.of(
                cat(null, 0L, "餐饮", 0, "🍔", "#ff6b35", 1),
                cat(null, 0L, "交通", 0, "🚌", "#5b9bd5", 2),
                cat(null, 0L, "购物", 0, "🛍", "#ff6b8a", 3),
                cat(null, 0L, "娱乐", 0, "🎮", "#a29bfe", 4),
                cat(null, 0L, "居家", 0, "🏠", "#fdcb6e", 5),
                cat(null, 0L, "通讯", 0, "📱", "#00b894", 6),
                cat(null, 0L, "医疗", 0, "💊", "#e17055", 7),
                cat(null, 0L, "教育", 0, "📚", "#00cec9", 8),
                cat(null, 0L, "人情", 0, "🎁", "#fd79a8", 9),
                cat(null, 0L, "其他", 0, "💸", "#636e72", 10),
                cat(null, 0L, "工资", 1, "💰", "#00b894", 1),
                cat(null, 0L, "兼职", 1, "💼", "#5b9bd5", 2),
                cat(null, 0L, "理财", 1, "📈", "#fdcb6e", 3),
                cat(null, 0L, "红包", 1, "🧧", "#ff6b6b", 4),
                cat(null, 0L, "退款", 1, "↩️", "#00cec9", 5),
                cat(null, 0L, "其他", 1, "💵", "#636e72", 6));

        categoryService.saveBatch(categories);
    }

    private Category cat(Long bookId, Long parentId, String name, int type, String icon, String color, int sortOrder) {
        Category c = new Category();
        c.setBookId(bookId);
        c.setParentId(parentId);
        c.setName(name);
        c.setType(type);
        c.setIcon(icon);
        c.setColor(color);
        c.setSortOrder(sortOrder);
        return c;
    }
}
