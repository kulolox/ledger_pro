# LedgerPro 后端工程规范 (`back_end`)

本项目为基于 **Spring Boot + Java 21 + MyBatis-Plus 3.5 + Spring Security + MySQL 8 + Redis 7** 的记账服务端。

---

## 1. 核心架构与分层设计

* **标准分层**：
  `Controller` $\rightarrow$ `IService<Entity>` $\rightarrow$ `ServiceImpl<Mapper, Entity>` $\rightarrow$ `BaseMapper<Entity>`
* **查询规范**：
  * 优先使用 MyBatis-Plus 提供的 `lambdaQuery()` 与 `lambdaUpdate()` 链式构建安全查询，禁止手动字符串拼接列名。
  * 复杂聚合报表或特定多表查询，在 XML 中定义 SQL，且必须使用 `#{param}` 参数绑定。
* **批量操作格式**：
  * 批量新增/修改统一包裹在 `{ "items": [...] }`。
  * 批量删除统一包裹在 `{ "ids": [...] }`。
* **数据隔离与归属权校验（强红线）**：
  * 所有与交易相关的增删改查必须显式校验 `book_id` 归属当前登录用户（从 `AuthUtils.getCurrentUserId()` 提取上下文）。
  * 逻辑删除：所有业务表必须有 `deleted` 标记，由 MyBatis-Plus 框架自动过滤。

---

## 2. 金额与时间规范

* **金额存储**：
  * 实体类字段使用 `Integer amount`，数据库类型为 `INT`，单位一律为**“分”**。
  * 严禁在数据库使用浮点型（`FLOAT`/`DOUBLE`）存储资金。
* **时间处理**：
  * 交易时间 `trans_time` 精确到秒，统一采用 `LocalDateTime`。
  * 必须配置格式化适配器，同时兼容 ISO-8601 带 `T` 和带空格的日期时间格式（如 `yyyy-MM-dd HH:mm:ss` 与 `yyyy-MM-dd'T'HH:mm:ss`）。

---

## 3. 认证、安全与防御

* **Spring Security 踩坑指南**：
  * 当配置了 `server.servlet.context-path=/api/v1` 时，Security 配置类的 `requestMatchers()` 匹配路径**不含** context-path。配置 `requestMatchers("/auth/**")` 即可匹配 `/api/v1/auth/**`，切忌重复拼接。
  * `JwtAuthFilter` **严禁标注 `@Component`**（否则会被 Spring Boot 容器与 Security Filter Chain 重复注册两次引发过滤器冲突），必须在 `SecurityConfig` 中通过 `@Bean` 注入。
* **密码加解密**：
  * 客户端提交经 RSA 公钥加密的密码密文。
  * 后端解密：使用 RSA 私钥解密出明文后，再通过 Spring Security `BCryptPasswordEncoder` 进行加盐哈希比对或存储。
* **防护拦截**：
  * 参数校验：所有请求 DTO 必须标注 Jakarta Validation 注解（`@NotBlank`, `@NotNull`, `@Size` 等），Controller 入参加 `@Valid`。
  * XSS 与注入防护：通过 `SanitizeUtils` 对用户输入的 `remark`、`category` 等字段进行 HTML 实体转义存储；CSV 导出时处理 `=`/`+`/`-`/`@` 特殊前缀。

---

## 4. 缓存与分类隔离架构

* **交易缓存策略**：
  * 查询缓存：`@Cacheable(value="transactions", key="T(...AuthUtils).getCurrentUserId() + '::' + #bookId + '::' + #month")`，带租户隔离，TTL 为 30 分钟。
  * 写操作淘汰：新增、修改、导入、删除时必须通过程序化 `evictTransactionCache(bookId)` 精确淘汰当前用户的当前账本缓存，严禁使用 `@CacheEvict(allEntries=true)` 误伤全局用户缓存。
* **分类双表设计**：
  * `category` 表：存系统初始模板（16 条，只读不可变）。
  * `user_category` 表：用户独立数据（`source_id` 指向系统模板或为 null，支持用户个性化排序 `sort_order` 与显隐 `visible`）。
  * 业务操作必须面向 `user_category`，确保多租户隔离。

---

## 5. 异常处理与统一响应

* **统一返回**：HTTP 状态码固定为 200，包体为标准 `Result<T>`。
  * 成功：`Result.success(data)`（`code: 0`）。
  * 业务失败：抛出 `BizException(code, message)`，由 `GlobalExceptionHandler` 捕获返回 `Result.fail(code, message)`。
  * 系统未知异常：记录完整 SLF4J 堆栈，返回通用 `code: 500` "服务器内部错误"，严禁向客户端泄露内部异常信息。

---

## 6. 验证与构建命令

```bash
mvn test                  # 执行所有后端单元与集成测试
mvn test-compile          # 校验编译语法与 Lombok/MapStruct 生成
mvn clean package         # 打包可执行 jar
mvn spotless:apply        # 代码自动对齐与格式化（提交前必须执行）
mvn spotless:check        # 格式规范检查
```
