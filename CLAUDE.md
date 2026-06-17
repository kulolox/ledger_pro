# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

记账软件 LedgerPro。前端 uni-app (Vue 3 + TS + Vite)，后端 Spring Boot + MyBatis-Plus + MySQL + Redis，JWT 双 token 认证，RSA 密码加密。

```
ledger_pro/
├── document/          # 产品需求、技术方案、迭代计划
├── front_end/         # uni-app 前端工程
└── back_end/          # Spring Boot 后端工程
```

## 常用命令

### 前端 (front_end/)

```bash
cd front_end
pnpm dev:h5          # 启动 H5 开发服务器
pnpm build:h5        # 构建 H5 生产包
pnpm type-check      # TypeScript 类型检查 (vue-tsc --noEmit)
```

### 后端 (back_end/)

```bash
cd back_end
mvn spring-boot:run              # 启动开发服务器
mvn test                         # 运行测试
mvn clean package                # 打包
mvn spotless:apply               # 代码格式化
```

## 架构关键点

### 前端

- **框架**: uni-app 3.0 + Vue 3 Composition API + TypeScript 4.9 + Vite 5
- **UI 库**: Wot UI (`@wot-ui/ui`, 组件前缀 `wd-`)
- **路径别名**: `@/` → `src/` (tsconfig paths)
- **请求封装**: `src/utils/request.ts` — 基于 `uni.request` 的拦截器模式 Request 类，自动注入 Bearer token、响应解析、错误 toast。默认实例从 `VITE_API_BASE_URL` 读 baseURL
- **密码加密**: `src/utils/crypto.ts` — RSA 公钥加密，登录/注册前加密密码
- **页面路由**: `src/pages.json` 管理，easycom 自动扫描组件
- **当前页面**: `pages/index/index` (首页)、`pages/login/index` (登录)、`pages/statistics/index` (统计)、`pages/category/index` (分类管理)、`pages/import-export/index` (导入导出)
- **拖拽排序**: `sortablejs` 库，分类管理页拖拽排序

### 后端

- **框架**: Spring Boot 4.0.6 + Java 21 + MyBatis-Plus 3.5.16 + Spring Security
- **数据库**: MySQL 8.0 (jdbc:mysql://localhost:3306/ledger_pro, root/123456)
- **缓存**: Redis 7 (localhost:6379, Lettuce 连接池), Spring Cache 抽象，自定义 `JacksonSerializer` JSON 序列化（`@type`/`@value` 包装格式，类白名单反序列化）
- **ORM**: MyBatis-Plus，逻辑删除由 `global-config.db-config.logic-delete-field` 自动处理
- **认证**: JWT (jjwt 0.12.5, HS256)，access token 24h + refresh token 7d 滑动过期，`type` claim 严格区分。`validateToken` 要求 type="access"，`validateRefreshToken` 要求 type="refresh"。JWT_SECRET 必须通过环境变量配置（无默认值，未配置则启动报错）。退出登录时同时拉黑 access token 和 refresh token，JwtAuthFilter 每次请求校验黑名单
- **密码**: BCrypt + RSA 加密传输 (前端公钥加密 → 后端私钥解密 → BCrypt)。RSA 密钥对持久化到 `back_end/data/`（首次启动生成，后续复用），已加入 `.gitignore`
- **代码格式**: Spotless (Palantir Java Format) + EditorConfig，`mvn spotless:apply` 手动格式化
- **分层**: Controller → Service Interface (IService) → Service Impl (ServiceImpl) → Mapper (BaseMapper)

### Spring Security 配置要点

- **路径匹配**: `server.servlet.context-path=/api/v1` 时，Spring Security 用 `MvcRequestMatcher` 匹配，路径**不含** context-path 前缀。SecurityConfig 中 `requestMatchers("/auth/**")` 匹配 `/api/v1/auth/**`，`requestMatchers("/**")` 匹配其余所有。不要写成 `/api/v1/**`
- **JwtAuthFilter**: 不能用 `@Component`（会导致 Spring Boot 自动注册为 Servlet Filter，与 Security 链内的 filter 冲突），必须去 `@Component`，在 SecurityConfig 中以 `@Bean` 方式创建
- **401 响应**: 配置 `AuthenticationEntryPoint` 返回 HTTP 401 + JSON `{"code":401,"message":"未登录"}`，前端 `request.ts` 据此触发 refresh token 流程
- **前端 refresh 流程**: `request.ts` catch 块 `err.statusCode === 401` → `tryRefreshToken()` → POST `/api/v1/auth/refresh` → 拿到新 token → 重试请求。refresh 失败则清 token 跳转登录页
- **categoryStore 的坑**: `fetchFromServer` 用原始 `uni.request`（非 request 实例），不会自动带 Authorization header，需要手动从 storage 取 token 加入 header

### 导入导出

- **后端**: `GET /transactions/export?book_id&month&format=[csv|excel|json]` 导出当月账单，`POST /transactions/import` (multipart) 导入
- **导出格式**: `type` 显示"支出/收入"文本、`amount` 显示元（保留两位小数）、`category` 显示分类名称、`trans_time` 显示 `yyyy-MM-dd HH:mm:ss`
- **导入兼容**: 同时支持新旧格式——type 文本/数字、amount 元/分自动检测、category 名称查 ID 或直接传数字、表头兼容 `category` 和 `category_id`
- **依赖**: Apache POI (poi-ooxml 5.4.0) 处理 Excel、OpenCSV 5.10 处理 CSV、jackson-databind 处理 JSON
- **前端页**: `pages/import-export/index`，两个 tab（导出/导入），导出触发浏览器下载（fetch blob + `<a>` 标签），导入走 `uni.chooseFile` + `request.upload`
- **通道**: `request.download(url, params)` — fetch + blob + download link；`request.upload(url, filePath, formData)` — `uni.uploadFile` 封装

### 邮件发送

- **MailService**: `sendResetCode(toEmail, code)` 发送密码重置验证码，QQ SMTP (smtp.qq.com:587)
- **关键配置**: `spring.mail.username` 和 `mail.smtp.from` 必须一致（均为 QQ 邮箱地址），否则 501 "Mail from address must be same as authorization user"
- **发送顺序**: 先 `mailSender.send(msg)` 发送成功后再 `stringRedisTemplate` 存 Redis，避免发送失败导致验证码被占用无法重试
- **配置位置**: 敏感值（DB/邮件/JWT）通过环境变量注入，`application.yaml` 用 `${VAR:}` 占位符引用

### 异常处理

- **GlobalExceptionHandler**: `@RestControllerAdvice`，所有异常返回 HTTP 200 + `Result.fail(code, message)`，不设 `@ResponseStatus`（避免 HTTP 状态码触发前端 HttpError 分支吞掉业务错误信息）
- **BizException**: 返回业务错误码和消息，前端 `request.ts` BusinessError 分支读取 `data.message` 并 toast
- **MethodArgumentNotValidException**: 校验失败，返回字段级错误详情（需 `@Valid` + Jakarta 约束注解）
- **RuntimeException**: code=400，返回通用提示，不泄露 `e.getMessage()`，通过 SLF4J 记录完整堆栈
- **Exception**: code=500，"服务器内部错误"，不泄露异常细节
- **前端 request.ts 拦截器**: HTTP status ≥ 300 → HttpError 路径（状态码错误）；HTTP 200 + `data.code !== 0` → BusinessError 路径（显示 `data.message`）
- **429 限流响应**: 速率限制拦截器返回 `{"code":429,"message":"请求过于频繁，请稍后再试"}`，前端 BusinessError 分支统一 toast

### 缓存策略

- **交易列表缓存**: `TransactionServiceImpl.list()` → `@Cacheable(value="transactions", key="T(...AuthUtils).getCurrentUserId() + '::' + #bookId + '::' + #month")`，key 含 userId 实现租户隔离，30min TTL
- **写操作淘汰**: `create/createBatch/import/batchDelete` → 程序化 `evictTransactionCache(bookId)` 精确淘汰当前用户的当月缓存，不再用 `@CacheEvict(allEntries=true)`（会清空所有用户缓存）
- **配置**: `spring.cache.type=redis`，`RedisCacheManager` Bean 配置自定义 `JacksonSerializer`（`@type`/`@value` JSON 包装，解决 Jackson `@class` 类型信息缺失问题）
- **序列化**: `TransactionListResponse` record 实现 `Serializable`，缓存值以 JSON 存入 Redis
- **分类 ETag 缓存**: `GET /categories` 响应 `ETag` 头，前端 `If-None-Match` 协商缓存，`categoryStore` 本地 storage 缓存 + ETag 双重缓存。注意：`fetchFromServer` 用原始 `uni.request`，需手动加 Authorization header

### 关键约定

- **金额存储**: 一律用 `INT` 存储"分"（避免浮点精度问题），前后端传输时注意 ×100/÷100 转换
- **API 规范**: RESTful，JSON，版本前缀 `/api/v1/`，JWT Bearer token
- **多账本隔离**: 交易数据通过 `bookId` 参数隔离 + `book_id` 归属权校验（防止水平越权）。BookId 随机生成（8-12 位数字，`IdType.INPUT`），避免自增泄露账本数量
- **逻辑删除**: 核心表用 `deleted` 字段标记，不物理删除。MyBatis-Plus 自动处理，查询/更新自动追加 `deleted=0`
- **交易时间**: `trans_time` 精确到秒。前端 `TransactionEditor` 构造 `YYYY-MM-DDThh:mm:ss`（当前时分秒），后端 `toEntity` 解析 `LocalDateTime.parse(time.replace(" ", "T").substring(0, 19))`
- **RSA 加密**: 前端获取公钥 → Web Crypto API (RSA-OAEP SHA-256) 加密 → 后端 RSA 解密 → BCrypt 验证。`GET /api/v1/auth/public-key`。密钥持久化到 `data/`，前端 `cachedPublicKey` 缓存（重启后需清除）

### 分类管理（用户隔离架构）

- **双表设计**: `category` 表仅存系统模板（16 条，不可变），`user_category` 表存每用户独立数据（可见性、排序、自定义分类）
- **延迟初始化**: 用户首次请求分类时 `UserCategoryServiceImpl.initForUser(userId)` 从 `category` 模板复制到 `user_category`，`source_id` 指向模板 ID
- **系统分类**: `is_system=1`，不可编辑名称/图标/颜色、不可删除，可调整排序和可见性（仅改 `user_category` 表）
- **自定义分类**: `is_system=0, source_id=null`，用户可增删改、排序、切换可见性。删除按 ID 精确删除，不按名称
- **租户隔离**: 所有 `user_category` 查询/更新/删除均带 `user_id` 条件，用户操作不影响其他用户
- **排序**: `sort_order` 字段，`listAll()` 按 `sort_order ASC` 排序，`PUT /categories/reorder` 批量更新排序（仅更新当前用户的记录）
- **可见性**: `visible` 字段，`visible=0` 的分类在记账页不显示，分类管理页半透明标示
- **数据初始化**: `DataInitializer` 在 `category` 空表时插入 16 个系统分类（10 支出 + 6 收入），ID 与前端 `DEFAULT_CATEGORIES` 一致
- **离线兜底**: `DEFAULT_CATEGORIES` 在服务端获取失败时作 fallback，正常流程走 `categoryStore.loadCategories()` + ETag 协商缓存
- **Service 层**: `IUserCategoryService` / `UserCategoryServiceImpl` 处理所有分类业务，`ICategoryService` / `CategoryServiceImpl` 仅用于 DataInitializer 初始化模板

### 安全配置

- **输入验证**: `spring-boot-starter-validation`，所有 request DTO 使用 Jakarta 约束注解（`@NotBlank/@Email/@Size/@NotNull/@NotEmpty`），Controller 参数加 `@Valid`
- **速率限制**: `RateLimitInterceptor` + Redis 计数器，按 IP 限流 `/auth/**`（login 10/min, register 5/min, reset-code 3/min, reset-pwd 5/min），`WebMvcConfig` 注册
- **CORS**: `SecurityConfig.corsConfigurationSource()` Bean，允许所有来源（`*`），`GET/POST/PUT/DELETE/OPTIONS`
- **安全响应头**: `X-XSS-Protection: 1; mode=block`、`X-Content-Type-Options: nosniff`、`X-Frame-Options: DENY`
- **XSS 防护**: `SanitizeUtils.escapeHtml()` 对用户输入（remark/nickname/category name/icon/color）HTML 实体转义后存储
- **CSV 注入防护**: `SanitizeUtils.sanitizeCsvCell()` 对 `=`/`+`/`-`/`@` 开头的导出单元格加单引号前缀
- **路径遍历防护**: 导出文件名中 `\ / : * ? \" < > |` 替换为 `_`
- **复位码恒定时间比较**: `MessageDigest.isEqual()` 防止时序攻击
- **JacksonSerializer 白名单**: 反序列化仅允许 `TransactionListResponse` / `TransactionResponse` 类型，拒绝未知类
- **文件上传限制**: `spring.servlet.multipart.max-file-size=5MB, max-request-size=10MB`
- **密钥管理**: 所有敏感值通过环境变量注入（`DB_USERNAME/DB_PASSWORD/MAIL_USERNAME/MAIL_PASSWORD/MAIL_FROM/JWT_SECRET`），`application.yaml` 用 `${VAR:}` 占位符

## 规范

### 前端规范

- 可以使用 wotui 组件的，用组件，不要自己实现
- 样式规范: 禁止使用颜色硬编码，scss 样式采用嵌套写法
- 注释: 添加必要注释，保证代码可读性
- 纯函数需要提取放在 utils 下
- 通用 ts 声明放在 types 下

### 同步策略

本地优先 (local-first)，服务端为备份：

```
pushDeletes → pushPending → pullFromServer
```

- **pushDeletes**: 收集本地 `deleted=1 + sync_status=pending + 有 server_id` 的记录，批量 POST `/batch-delete`
- **pushPending**: 收集本地 `sync_status=pending + deleted=0` 的记录，批量 POST `/batch`（注意：data 必须用 `{items: [...]}` 对象包裹，uni-app H5 不支持裸数组序列化）
- **pullFromServer**: 拉取当月全量数据（传 `book_id` + `month` YYYY-MM），按 server_id 去重合并
- 同步失败**不修改本地数据状态**，保持 pending 等待下次重试
- 同步锁 `syncing` 防止并发；定时轮询 30 分钟 + 网络恢复触发
- `scheduleSync` 注册 `onNetworkStatusChange` 监听器，`stopSync` 必须用 `offNetworkStatusChange` 清理，否则累积监听器
- `pullFromServer` 用 `pullPromise` 飞行中跟踪，重复调用返回已有 Promise，完成后 `finally` 清空

### 后端 Controller 模式

- 批量接口统一包裹格式：`{items: [...]}` 或 `{ids: [...]}`
- 时间解析兼容空格和 T 分隔符：`transTimeStr.replace(" ", "T")`
- month 查询：前端传 `YYYY-MM`，后端拼 `month + "-01T00:00:00"` 然后 `plusMonths(1)` 做范围查询
- 软删除：使用 MyBatis-Plus `UpdateWrapper.set("deleted", 1)` 批量更新

### Service 层模式

- 接口继承 `IService<Entity>`，定义业务方法
- 实现类继承 `ServiceImpl<Mapper, Entity>` 实现接口，获得内置 CRUD
- `lambdaQuery()` 链式调用替代手动构建 QueryWrapper
- 批量插入使用 `saveBatch()`，批量更新使用 `UpdateWrapper` + `update(wrapper)`

### 数据隔离与安全

- **交易归属校验**: `TransactionServiceImpl` 所有接口（create/list/export/import/batchDelete）均校验 `book_id` 归属当前用户（`validateBookOwnership`），防止水平越权（IDOR）
- **账户归属校验**: `AccountServiceImpl.listByBookId` 校验账本 `ownerId` 匹配当前用户
- **BookId 随机化**: `AccountBook.id` 用 `IdType.INPUT` + `ThreadLocalRandom` 生成 8-12 位随机数字，避免自增泄露账本数量。`generateBookId()` 循环检查唯一性
- **分类接口**: 通过 `user_category` 表 `user_id` 隔离，不传 `book_id`
- **前端退出清理**: `logout()` → `clearAllData()` 清除全部 8 个 storage key（token/refresh_token/user/book/transactions/category_cache/category_etag/sync_last_at），切换账号数据不残留
- **前端 `currentBookId` 校验**: 首页 onShow 检查当前 bookId 是否仍在 books 列表中，不在则回退到第一个账本
- **前端 `dayGroups`** 计算属性按 `currentBookId` 过滤
- `currentBookId` 持久化到 storage，下次启动恢复
