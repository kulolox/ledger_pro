package com.example.back_end.controller;

import com.example.back_end.dto.request.CategoryRequest;
import com.example.back_end.dto.response.CategoryResponse;
import com.example.back_end.service.IUserCategoryService;
import com.example.back_end.utils.Result;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final IUserCategoryService userCategoryService;

    public CategoryController(IUserCategoryService userCategoryService) {
        this.userCategoryService = userCategoryService;
    }

    @GetMapping
    public ResponseEntity<Result<List<CategoryResponse>>> list(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        List<CategoryResponse> categories = userCategoryService.listAll();
        String etag = computeEtag(categories);
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.ok().eTag(etag).body(Result.ok(categories));
    }

    @PostMapping
    public Result<CategoryResponse> create(@Valid @RequestBody CategoryRequest req) {
        return Result.ok("添加成功", userCategoryService.create(req));
    }

    @PutMapping("/{id}")
    public Result<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return Result.ok("修改成功", userCategoryService.update(id, req));
    }

    @PutMapping("/reorder")
    public Result<Void> reorder(@RequestBody Map<String, List<Long>> body) {
        userCategoryService.reorder(body.get("ids"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userCategoryService.delete(id);
        return Result.ok("删除成功", null);
    }

    private String computeEtag(List<CategoryResponse> categories) {
        StringBuilder sb = new StringBuilder();
        for (CategoryResponse c : categories) {
            sb.append(c.id())
                    .append(':')
                    .append(c.name())
                    .append(':')
                    .append(c.type())
                    .append(':')
                    .append(c.icon())
                    .append(':')
                    .append(c.color())
                    .append(':')
                    .append(c.parent_id())
                    .append(':')
                    .append(c.is_system())
                    .append(':')
                    .append(c.sort_order())
                    .append(':')
                    .append(c.visible())
                    .append(',');
        }
        return String.valueOf(Math.abs(sb.toString().hashCode()));
    }
}
