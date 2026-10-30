package com.example.back_end.config;

import com.example.back_end.entity.Category;
import com.example.back_end.service.ICategoryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ICategoryService categoryService;

    public DataInitializer(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String @NonNull ... args) {
        // CommandLineRunner 启动回调：表非空则跳过，保证重复启动幂等
        if (categoryService.count() > 0) {
            log.info("分类模板已存在，跳过初始化");
            return;
        }

        List<Category> categories = List.of(
                cat("餐饮", 0, "🍔", "#ff6b35", 1),
                cat("交通", 0, "🚌", "#5b9bd5", 2),
                cat("购物", 0, "🛍", "#ff6b8a", 3),
                cat("娱乐", 0, "🎮", "#a29bfe", 4),
                cat("居家", 0, "🏠", "#fdcb6e", 5),
                cat("通讯", 0, "📱", "#00b894", 6),
                cat("医疗", 0, "💊", "#e17055", 7),
                cat("教育", 0, "📚", "#00cec9", 8),
                cat("人情", 0, "🎁", "#fd79a8", 9),
                cat("其他", 0, "💸", "#636e72", 10),
                cat("工资", 1, "💰", "#00b894", 1),
                cat("兼职", 1, "💼", "#5b9bd5", 2),
                cat("理财", 1, "📈", "#fdcb6e", 3),
                cat("红包", 1, "🧧", "#ff6b6b", 4),
                cat("退款", 1, "↩️", "#00cec9", 5),
                cat("其他", 1, "💵", "#636e72", 6));

        categoryService.saveBatch(categories);
        log.info("分类模板初始化完成，共 {} 条", categories.size());
    }

    private Category cat(String name, int type, String icon, String color, int sortOrder) {
        Category c = new Category();
        c.setParentId(0L);
        c.setName(name);
        c.setType(type);
        c.setIcon(icon);
        c.setColor(color);
        c.setSortOrder(sortOrder);
        c.setIsSystem(1);
        return c;
    }
}
