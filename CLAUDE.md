# CLAUDE.md

## 项目概述

记账软件 LedgerPro。前端 uni-app (Vue 3 + TS + Vite)，后端 Spring Boot + MyBatis-Plus + MySQL + Redis，JWT 双 token 认证，RSA 密码加密。

```
ledger_pro/
├── document/          # specs/ design/ plans/ reports/ images/
├── front_end/         # uni-app 前端工程
└── back_end/          # Spring Boot 后端工程
```

> 详细架构文档 → `document/design/architecture.md`（Docker部署、Spring Security、导入导出、缓存策略、E2E测试等）

## 常用命令

### 前端 (front_end/)

```bash
cd front_end
pnpm dev:h5          # 启动 H5 开发服务器
pnpm build:h5        # 构建
pnpm type-check      # TypeScript 类型检查
pnpm test:e2e        # Playwright E2E
pnpm test:unit       # Vitest 单元测试
```

### 后端 (back_end/)

```bash
cd back_end
mvn spring-boot:run              # 启动
mvn test                         # 测试
mvn clean package                # 打包
mvn spotless:apply               # 代码格式化
```

## 架构要点

### 前端

- **框架**: uni-app 3.0 + Vue 3 Composition API + TypeScript + Vite 5
- **UI 库**: Wot UI (`@wot-ui/ui`, 组件前缀 `wd-`)
- **路径别名**: `@/` → `src/`
- **请求封装**: `src/utils/request.ts` — Request 类，自动注入 Bearer token、响应解析、错误 toast
- **密码加密**: `src/utils/crypto.ts` — RSA 公钥加密
- **页面**: `pages/index/index` (首页)、`pages/login/index` (登录)、`pages/statistics/index` (统计)、`pages/category/index` (分类管理)、`pages/import-export/index` (导入导出)

### 后端

- **框架**: Spring Boot 4.0 + Java 21 + MyBatis-Plus 3.5 + Spring Security
- **DB**: MySQL 8.0 (ledger_pro)，Redis 7
- **认证**: JWT (jjwt, HS256)，access token 24h + refresh token 7d，`type` claim 区分
- **密码**: BCrypt + RSA 加密传输（前端公钥加密 → 后端私钥解密）
- **分层**: Controller → Service Interface → Service Impl → Mapper
- **API**: RESTful，JSON，`/api/v1/`

## 关键约定

- **金额存储**: 一律 `INT` 存"分"，前后端传输 ×100/÷100
- **多账本隔离**: 交易通过 `bookId` 隔离 + `book_id` 归属权校验。BookId 随机 8-12 位数字
- **逻辑删除**: `deleted` 字段，MyBatis-Plus 自动处理
- **交易时间**: `trans_time` 精确到秒，前后端 T/空格分隔符兼容

## 前端规范

- 优先用 wotui 组件，不自己实现
- SCSS 嵌套写法，禁止颜色硬编码
- 纯函数提取到 utils，通用类型声明放 types
- 添加必要注释

## 同步策略

本地优先：`pushDeletes → pushPending → pullFromServer`，失败不修改本地数据，`syncing` 锁防并发。

## 后端模式

- 批量接口包裹：`{items: [...]}` 或 `{ids: [...]}`
- Service: `IService<Entity>` → `ServiceImpl<Mapper, Entity>`，`lambdaQuery()` 链式查询
- 所有交易接口校验 `book_id` 归属当前用户
- 异常返回 HTTP 200 + `Result.fail(code, message)`
