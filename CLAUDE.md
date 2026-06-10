# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

记账软件 LedgerPro。前端 uni-app (Vue 3 + TS + Vite)，后端 Spring Boot + MyBatis-Plus + MySQL，JWT 认证。

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
- **页面路由**: `src/pages.json` 管理，easycom 自动扫描组件
- **当前页面**: `pages/index/index` (首页/记账页)、`pages/login/index` (登录页)

### 后端

- **框架**: Spring Boot 4.0.6 + Java 21 + MyBatis-Plus 3.5.16 + Spring Security
- **数据库**: MySQL 8.0 (jdbc:mysql://localhost:3306/ledger_pro, root/123456)
- **ORM**: MyBatis-Plus，逻辑删除由 `global-config.db-config.logic-delete-field` 自动处理
- **认证**: JWT (jjwt 0.12.5, HS256)，24 小时过期
- **密码**: BCrypt
- **代码格式**: Spotless (Palantir Java Format) + EditorConfig，`mvn spotless:apply` 手动格式化
- **分层**: Controller → Service Interface (IService) → Service Impl (ServiceImpl) → Mapper (BaseMapper)

### 关键约定

- 不要做多余的事
- **金额存储**: 一律用 `INT` 存储"分"（避免浮点精度问题），前后端传输时注意 ×100/÷100 转换
- **API 规范**: RESTful，JSON，版本前缀 `/api/v1/`，JWT Bearer token
- **多账本隔离**: 所有数据接口通过 `bookId` 参数隔离，后端结合当前用户鉴权
- **逻辑删除**: 核心表用 `deleted` 字段标记，不物理删除。MyBatis-Plus 自动处理，查询/更新自动追加 `deleted=0`
- **一期目标**: 登录认证 + 支出/收入/转账记账 + 交易历史列表。账户、分类暂为系统预置

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

- 所有接口通过 `book_id` 隔离数据
- 前端 `dayGroups` 计算属性按 `currentBookId` 过滤
- `currentBookId` 持久化到 storage，下次启动恢复
- 记账时 `book_id` 取自当前选中账本
