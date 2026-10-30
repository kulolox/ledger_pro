---
name: api-workflow
description: 当在 LedgerPro 中新增或修改 API 接口、需要同步推进后端 Spring Boot 实现与前端 TS 类型/请求封装时使用此技能。
---

# LedgerPro API 端到端开发工作流

本技能用于规范前后端接口契约对接，防止金额单位混乱、权限遗漏及类型脱节。

## 开发流程与检查清单

### 第一步：契约对齐
1. 查阅 [document/specs/一期接口清单.md](../../../document/specs/一期接口清单.md) 确认接口路径与出入参。
2. 确认核心规则：
   * 金额字段命名（入参 `amount` 单位：**分**，`Integer` 类型）。
   * 必须校验交易涉及的 `book_id` 归属当前登录用户。
   * 统一响应格式：`Result<T>`。

### 第二步：后端实现 (`back_end`)
1. **DTO 声明**：
   * 请求对象加 Jakarta 注解（如 `@NotNull`, `@Min(1)`, `@Size`）。
2. **Controller 暴露**：
   * 路径遵循 `/api/v1/xxx`。
   * 方法参数标注 `@Valid @RequestBody`。
   * 严禁在 Controller 堆叠业务，直接调用 Service。
3. **Service 业务逻辑**：
   * 鉴权核验：通过 `AuthUtils.getCurrentUserId()` 校验 `book_id` 所有权。
   * 异常抛出：业务失败统一抛出 `BizException(code, message)`。
   * 缓存处理：写操作后调用 `evictTransactionCache(bookId)`。
4. **单测验证**：
   * 编写 JUnit 5 + Mockito 测试用例验证正常流与异常流。
   * 执行 `mvn test-compile` 及 `mvn test`。
   * 执行 `mvn spotless:apply`。

### 第三步：前端对接 (`front_end`)
1. **类型定义 (`src/types/`)**：
   * 声明 Request 与 Response 的 TypeScript Interface。
2. **请求函数 (`src/api/` 或对应页面)**：
   * 引入 `src/utils/request.ts`。
   * 调用接口传参时：如果用户输入的是元，必须在此处乘以 100 转为分传给后端。
   * 接收数据渲染时：将接口返回的分除以 100 格式化。
3. **类型门禁**：
   * 执行 `pnpm type-check` 保证 0 错误。
