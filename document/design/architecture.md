# 架构详细文档

> 从 CLAUDE.md 移出的详细架构信息，按需查阅。

## Docker 部署

生产环境用 `docker-compose` 管理 4 个容器（mysql/redis/backend/nginx），所有数据挂载到 `D:/data_center/prod/`。

```bash
cd back_end
mvn clean package -DskipTests
docker-compose build --no-cache backend
docker-compose up -d
docker-compose down
```

- **nginx** 暴露 80 端口，反向代理 `/api/` → `backend:8080`
- **MySQL** 数据挂载 `D:/data_center/prod/mysql`，首次启动从 `docker/init.sql` 初始化
- **Redis** 数据挂载 `D:/data_center/prod/redis`
- **RSA 密钥** 挂载 `D:/data_center/prod/rsa-keys`
- **环境变量与多环境隔离**：
  - **生产环境 (prod)**：`docker-compose.yml` 自动指定 `SPRING_PROFILES_ACTIVE=prod`，加载 `application-prod.yaml`，服务端口为 8080，数据库/Redis 自动解析容器服务名 `mysql`/`redis`；敏感配置直接读取 `back_end/.env`。
  - **研发环境 (dev)**：本地命令行直接启动（`mvn spring-boot:run`）默认激活 `dev` profile，加载 `application-dev.yaml`，服务端口为 8114，默认连接 `127.0.0.1:3306` 与 `127.0.0.1:6379`。开发者可在 `back_end/.env.local` 中定制本地连接，优先级高于 `.env` 且被 `.gitignore` 自动忽略，实现两套环境彻底互不干涉。
  - **环境变量参考**：生产环境变量参考 `.env.example`，本地研发环境参考 `.env.local.example`。

### 编码陷阱

- **MySQL init.sql 客户端编码**: MySQL Docker 初始化脚本由 `mysql` CLI 执行，**CLI 默认 charset=latin1**，必须 `SET NAMES utf8mb4;` 写在 init.sql 最开头，否则中文/emoji 写入时乱码
- **Alpine JVM 编码**: `eclipse-temurin:21-jre-alpine` 镜像无完整 locale，JVM 默认可能非 UTF-8，Dockerfile 必须 `-Dfile.encoding=UTF-8`
- **nginx charset**: 加 `charset utf-8;` + `charset_types application/json;` 确保响应头带 charset
- **Spring 编码**: `server.servlet.encoding.charset=UTF-8` + `force=true` 兜底

### 前端代理配置

开发时 vite proxy `/api` → `http://127.0.0.1`（nginx 80 端口），**target 不要带 `/api` 后缀**，否则路径重复 `/api/api/v1/...` → 404。

## Spring Security 配置要点

- **路径匹配**: `server.servlet.context-path=/api/v1` 时，Spring Security 用 `MvcRequestMatcher` 匹配，路径**不含** context-path 前缀。SecurityConfig 中 `requestMatchers("/auth/**")` 匹配 `/api/v1/auth/**`，`requestMatchers("/**")` 匹配其余所有。不要写成 `/api/v1/**`
- **JwtAuthFilter**: 不能用 `@Component`（会导致 Spring Boot 自动注册为 Servlet Filter，与 Security 链内的 filter 冲突），必须去 `@Component`，在 SecurityConfig 中以 `@Bean` 方式创建
- **401 响应**: 配置 `AuthenticationEntryPoint` 返回 HTTP 401 + JSON `{"code":401,"message":"未登录"}`，前端 `request.ts` 据此触发 refresh token 流程
- **前端 refresh 流程**: `request.ts` catch 块 `err.statusCode === 401` → `tryRefreshToken()` → POST `/api/v1/auth/refresh` → 拿到新 token → 重试请求。refresh 失败则清 token 跳转登录页
- **categoryStore 的坑**: `fetchFromServer` 用原始 `uni.request`（非 request 实例），不会自动带 Authorization header，需要手动从 storage 取 token 加入 header

## 导入导出

- **后端**: `GET /transactions/export?book_id&month&format=[csv|excel|json]` 导出当月账单，`POST /transactions/import` (multipart) 导入
- **导出格式**: `type` 显示"支出/收入"文本、`amount` 显示元（保留两位小数）、`category` 显示分类名称、`trans_time` 显示 `yyyy-MM-dd HH:mm:ss`
- **导入兼容**: 同时支持新旧格式——type 文本/数字、amount 元/分自动检测、category 名称查 ID 或直接传数字、表头兼容 `category` 和 `category_id`
- **依赖**: Apache POI (poi-ooxml 5.4.0) 处理 Excel、OpenCSV 5.10 处理 CSV、jackson-databind 处理 JSON
- **前端页**: `pages/import-export/index`，两个 tab（导出/导入），导出触发浏览器下载（fetch blob + `<a>` 标签），导入走 `uni.chooseFile` + `request.upload`
- **通道**: `request.download(url, params)` — fetch + blob + download link；`request.upload(url, filePath, formData)` — `uni.uploadFile` 封装

## 邮件发送

- **MailService**: `sendResetCode(toEmail, code)` 发送密码重置验证码，QQ SMTP (smtp.qq.com:587)
- **关键配置**: `spring.mail.username` 和 `mail.smtp.from` 必须一致（均为 QQ 邮箱地址），否则 501 "Mail from address must be same as authorization user"
- **发送顺序**: 先 `mailSender.send(msg)` 发送成功后再 `stringRedisTemplate` 存 Redis，避免发送失败导致验证码被占用无法重试

## 异常处理

- **GlobalExceptionHandler**: `@RestControllerAdvice`，所有异常返回 HTTP 200 + `Result.fail(code, message)`，不设 `@ResponseStatus`（避免 HTTP 状态码触发前端 HttpError 分支吞掉业务错误信息）
- **BizException**: 返回业务错误码和消息，前端 `request.ts` BusinessError 分支读取 `data.message` 并 toast
- **MethodArgumentNotValidException**: 校验失败，返回字段级错误详情（需 `@Valid` + Jakarta 约束注解）
- **RuntimeException**: code=400，返回通用提示，不泄露 `e.getMessage()`，通过 SLF4J 记录完整堆栈
- **Exception**: code=500，"服务器内部错误"，不泄露异常细节
- **前端 request.ts 拦截器**: HTTP status ≥ 300 → HttpError 路径（状态码错误）；HTTP 200 + `data.code !== 0` → BusinessError 路径（显示 `data.message`）
- **429 限流响应**: 速率限制拦截器返回 `{"code":429,"message":"请求过于频繁，请稍后再试"}`，前端 BusinessError 分支统一 toast

## 缓存策略

- **交易列表缓存**: `TransactionServiceImpl.list()` → `@Cacheable(value="transactions", key="T(...AuthUtils).getCurrentUserId() + '::' + #bookId + '::' + #month")`，key 含 userId 实现租户隔离，30min TTL
- **写操作淘汰**: `create/createBatch/import/batchDelete` → 程序化 `evictTransactionCache(bookId)` 精确淘汰当前用户的当月缓存，不再用 `@CacheEvict(allEntries=true)`（会清空所有用户缓存）
- **配置**: `spring.cache.type=redis`，`RedisCacheManager` Bean 配置自定义 `JacksonSerializer`（`@type`/`@value` JSON 包装，解决 Jackson `@class` 类型信息缺失问题）
- **序列化**: `TransactionListResponse` record 实现 `Serializable`，缓存值以 JSON 存入 Redis
- **分类 ETag 缓存**: `GET /categories` 响应 `ETag` 头，前端 `If-None-Match` 协商缓存，`categoryStore` 本地 storage 缓存 + ETag 双重缓存。注意：`fetchFromServer` 用原始 `uni.request`，需手动加 Authorization header

## 分类管理（用户隔离架构）

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

## 安全配置

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

## Playwright E2E 测试

### 配置

- `playwright.config.ts`: `channel: "chrome"` 使用本地 Chrome，baseURL `http://localhost:5173`，viewport 390×844 (iPhone 14 Pro)
- `webServer` 自动启动 `npx uni` (端口 5173, `reuseExistingServer` 复用已有服务)
- `.npmrc` 配置 `playwright_download_host = https://npmmirror.com/mirrors/playwright` 镜像加速

### 目录结构

```
front_end/tests/
├── setup.ts                          # 全局 setup
└── e2e/
    ├── fixtures/test.fixture.ts      # Worker 级 testUser 创建/销毁, authenticatedPage
    ├── page-objects/                 # Page Object 模式, 每页一个类
    │   ├── LoginPage.ts / RegisterPage.ts / ForgotPasswordPage.ts / HomePage.ts
    │   ├── CategoryPage.ts / StatisticsPage.ts / ImportExportPage.ts
    ├── specs/                        # 测试用例
    │   ├── auth.spec.ts              # 13 个: 登录/注册渲染/校验/导航/密码不匹配
    │   ├── forgot-password.spec.ts   # 5 个: 渲染/发送验证码/空码/返回/完整改密流程
    │   ├── home-transactions.spec.ts / categories.spec.ts / statistics.spec.ts / import-export.spec.ts / sidebar.spec.ts
    └── utils/
        ├── test-data.ts              # TEST_USER (动态 email 避免冲突), API_BASE
        └── db.ts                     # mysql2 直连: createTestUser/DestroyTestUser/getResetCode
```

### 关键点

- **路由格式**: uni-app H5 hash 模式，Page Object `goto()` 必须用 `/#/pages/...`，不用 `/pages/...`
- **App.vue onLaunch**: 只在 hash 为空时 reLaunch，否则覆盖直接访问的 register/forgot-password 页面
- **DB fixture**: Worker 级 `createTestUser()` → 所有 test 用完 → `destroyTestUser()`。category 表无 `deleted` 字段，不要加 WHERE deleted=0
- **login 后导航**: `uni.reLaunch("/pages/index/index")` 跳转到 `/#/` (首页是 pages.json 第一个)，waitForSelector(".home-page") 而非 waitForURL
- **navigateBack**: register/forgot-password 的"返回登录"用 `uni.navigateBack()`，测试需从 login navigateTo 过去才有返回栈
- **依赖**: `mysql2` (直连 DB) + `bcryptjs` (密码 hash) + `@playwright/test`
- **运行前提**: 后端容器 (mysql/redis/backend/nginx) 必须全部启动，前端 dev server (pnpm dev:h5) 运行中
