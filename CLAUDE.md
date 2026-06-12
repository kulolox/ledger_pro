# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

记账软件 LedgerPro。前端 uni-app (Vue 3 + TS + Vite)，后端 Spring Boot + MyBatis-Plus + MySQL + Redis，JWT 双 token 认证，RSA 密码加密。

```
daily_ledger/
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
- **当前页面**: `pages/index/index` (首页)、`pages/login/index` (登录)、`pages/statistics/index` (统计)、`pages/category/index` (分类管理)
- **拖拽排序**: `sortablejs` 库，分类管理页拖拽排序

### 后端

- **框架**: Spring Boot 4.0.6 + Java 21 + MyBatis-Plus 3.5.16 + Spring Security
- **数据库**: MySQL 8.0 (jdbc:mysql://localhost:3306/ledger_pro, root/123456)
- **缓存**: Redis 7 (localhost:6379, Lettuce 连接池), Spring Cache 抽象，自定义 `JacksonSerializer` JSON 序列化（`@type`/`@value` 包装格式）
- **ORM**: MyBatis-Plus，逻辑删除由 `global-config.db-config.logic-delete-field` 自动处理
- **认证**: JWT (jjwt 0.12.5, HS256)，access token 24h + refresh token 7d 滑动过期，`type` claim 区分。`validateToken` 拒绝 type=refresh 的 token，`validateRefreshToken` 接受 type=refresh 或老系统无 type 的 token（向后兼容）
- **密码**: BCrypt + RSA 加密传输 (前端公钥加密 → 后端私钥解密 → BCrypt)。RSA 密钥对持久化到 `back_end/data/`（首次启动生成，后续复用），已加入 `.gitignore`
- **代码格式**: Spotless (Palantir Java Format) + EditorConfig，`mvn spotless:apply` 手动格式化
- **分层**: Controller → Service Interface (IService) → Service Impl (ServiceImpl) → Mapper (BaseMapper)

### Spring Security 配置要点

- **路径匹配**: `server.servlet.context-path=/api/v1` 时，Spring Security 用 `MvcRequestMatcher` 匹配，路径**不含** context-path 前缀。SecurityConfig 中 `requestMatchers("/auth/**")` 匹配 `/api/v1/auth/**`，`requestMatchers("/**")` 匹配其余所有。不要写成 `/api/v1/**`
- **JwtAuthFilter**: 不能用 `@Component`（会导致 Spring Boot 自动注册为 Servlet Filter，与 Security 链内的 filter 冲突），必须去 `@Component`，在 SecurityConfig 中以 `@Bean` 方式创建
- **401 响应**: 配置 `AuthenticationEntryPoint` 返回 HTTP 401 + JSON `{"code":401,"message":"未登录"}`，前端 `request.ts` 据此触发 refresh token 流程
- **前端 refresh 流程**: `request.ts` catch 块 `err.statusCode === 401` → `tryRefreshToken()` → POST `/api/v1/auth/refresh` → 拿到新 token → 重试请求。refresh 失败则清 token 跳转登录页
- **categoryStore 的坑**: `fetchFromServer` 用原始 `uni.request`（非 request 实例），不会自动带 Authorization header，需要手动从 storage 取 token 加入 header

### 缓存策略

- **交易列表缓存**: `getTransactions` → `@Cacheable(value="transactions", key="#bookId + '::' + #month")`，30min TTL
- **写操作淘汰**: `create/createBatch/batchDelete` → `@CacheEvict(value="transactions", allEntries=true)`，清空全部交易缓存
- **配置**: `spring.cache.type=redis`，`RedisCacheManager` Bean 配置自定义 `JacksonSerializer`（`@type`/`@value` JSON 包装，解决 Jackson `@class` 类型信息缺失问题）
- **序列化**: `TransactionListResponse` record 实现 `Serializable`，缓存值以 JSON 存入 Redis
- **分类 ETag 缓存**: `GET /categories` 响应 `ETag` 头，前端 `If-None-Match` 协商缓存，`categoryStore` 本地 storage 缓存 + ETag 双重缓存。注意：`fetchFromServer` 用原始 `uni.request`，需手动加 Authorization header

### 关键约定

- 不要做多余的事
- **金额存储**: 一律用 `INT` 存储"分"（避免浮点精度问题），前后端传输时注意 ×100/÷100 转换
- **API 规范**: RESTful，JSON，版本前缀 `/api/v1/`，JWT Bearer token
- **多账本隔离**: 交易数据通过 `bookId` 参数隔离，分类全局共享不绑账本
- **逻辑删除**: 核心表用 `deleted` 字段标记，不物理删除。MyBatis-Plus 自动处理，查询/更新自动追加 `deleted=0`
- **RSA 加密**: 前端获取公钥 → Web Crypto API (RSA-OAEP SHA-256) 加密 → 后端 RSA 解密 → BCrypt 验证。`GET /api/v1/auth/public-key`。密钥持久化到 `data/`，前端 `cachedPublicKey` 缓存（重启后需清除）

### 分类管理

- **全局共享**: 分类不绑定账本，所有账本共用同一套分类
- **系统分类**: `is_system=1`，不可编辑名称/图标/颜色、不可删除，可调整排序和可见性
- **自定义分类**: `is_system=0`，用户可增删改、排序、切换可见性
- **排序**: `sort_order` 字段，`listAll()` 按 `sort_order ASC` 排序，`PUT /categories/reorder` 批量更新排序
- **可见性**: `visible` 字段，`visible=0` 的分类在记账页不显示，分类管理页半透明标示
- **数据初始化**: `DataInitializer` 在空表时插入 16 个系统分类（10 支出 + 6 收入），ID 与前端 `DEFAULT_CATEGORIES` 一致

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

### 数据隔离

- 交易接口通过 `book_id` 隔离数据
- 分类接口全局共享，不传 `book_id`
- 前端 `dayGroups` 计算属性按 `currentBookId` 过滤
- `currentBookId` 持久化到 storage，下次启动恢复
- 记账时 `book_id` 取自当前选中账本

### 前移的 status quo

分类从硬编码迁移到服务端：`DEFAULT_CATEGORIES` 仅作离线兜底，正常流程通过 `categoryStore.loadCategories()` 从服务端获取，ETag 协商缓存减少传输。
