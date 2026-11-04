# LedgerPro 系统架构与技术实现

本文档为 LedgerPro 全栈技术架构与核心机制说明。

---

## 一、系统总体架构

LedgerPro 采用**“前端本地优先（Local-First）+ 后端分层 REST 服务 + 容器化集群”**的极简高效架构：

```
[ uni-app 跨端前端 (Android / H5) ]
        │  (RSA 密码加密 / JWT 双 Token / ETag 缓存协商)
        ▼
[ Nginx 反向代理 (端口 80 / SSL) ]
        │
        ▼
[ Spring Boot 3 服务端 (Java 21, 端口 8080) ]
   ├── [ MySQL 8.0 ] (utf8mb4, 主键自增/雪花ID, 核心流水覆盖索引)
   └── [ Redis 7 ]   (租户隔离缓存, 验证码, IP 速率限流)
```

---

## 二、容器化部署架构 (`back_end/docker/`)

所有后端及依赖服务的容器编排配置均集中收敛于 `back_end/docker/` 目录下：

```
back_end/docker/
├── Dockerfile              # 后端 JRE 21 Alpine 轻量容器镜像
├── .dockerignore           # 构建上下文隔离规则
├── docker-compose.yml      # 生产环境 4 容器全编排 (MySQL / Redis / Backend / Nginx)
├── docker-compose.dev.yml  # 本地开发依赖编排 (仅 MySQL 3306 + Redis 6379)
├── init.sql                # 生产/研发环境数据库初始结构与系统模板数据
└── nginx/
    └── nginx.conf          # Nginx 反向代理与 HTTP 头部安全加固
```

### 1. 服务容器说明
- **Nginx**：对外暴露 80 端口，反向代理 `/api/` 路由至 `backend:8080`，统一注入 `charset utf-8` 与安全防护响应头；
- **MySQL 8.0**：生产容器数据卷挂载至宿主机 `data/mysql`，初始化从 `init.sql` 加载；
- **Redis 7**：生产容器启用 AOF 持久化与 `allkeys-lru` 内存淘汰机制；
- **Backend (Spring Boot)**：基于 `eclipse-temurin:21-jre-alpine`，注入 `-Dfile.encoding=UTF-8` 保证中文与 Emoji 正确处理，挂载 RSA 密钥目录并读取生产 `.env`。

### 2. 多环境与配置隔离原则
- **生产环境 (prod)**：`docker-compose.yml` 自动指定 `SPRING_PROFILES_ACTIVE=prod`，加载 `application-prod.yaml`，敏感参数直接注入远程 `/opt/ledger_pro/.env`；
- **研发环境 (dev)**：本地命令行启动（`mvn spring-boot:run`）默认激活 `dev` profile，加载 `application-dev.yaml`，端口为 8114。支持通过 `back_end/.env.local` 本地覆盖，被 `.gitignore` 自动隔离。

---

## 三、本地优先与离线同步机制 (Local-First Sync)

为了保障移动端极佳的离线可用性与记账瞬间响应，前端采用**本地存储先行、异步可靠同步**的闭环机制：

### 1. 记账流水生命周期与状态机
每笔本地流水具备 `sync_status` 状态字段：
- `pending`：已在本地持久化但尚未落入远端服务端；
- `synced`：已成功与服务端对齐入库；
- `failed`：网络异常或同步失败，留存本地并支持在下一次网络就绪时重试。

### 2. 标准同步流程（原子三步法）
为杜绝数据竞态与覆写冲突，前端 `sync.ts` 严格遵循互斥锁保护下的固定同步时序：
```
1. pushDeletes   (推送本地待删除的记录，保证远端先清理)
       │
2. pushPending   (批量推送本地未同步的流水，落库后标记为 synced)
       │
3. pullFromServer (从服务端拉取最新账单与缓存合并，服务端权威对齐)
```

---

## 四、多账本隔离与分类体系设计

### 1. 多账本物理/逻辑硬隔离
- 所有交易明细表必须包含 `book_id`；
- 后端任何流水查询、创建、编辑或删除操作，必须通过 `validateBookOwnership(bookId)` 强制比对当前 `AuthUtils.getCurrentUserId()`，严禁越权操作其他用户的账本。

### 2. 分类双表架构（系统模板 vs 用户定制）
- **`category` 表**：存储系统预设的 16 个标准化分类（10 支出 + 6 收入），全局只读不可变；
- **`user_category` 表**：每用户独立维护的数据副本（支持用户自定义增删改、设置显隐 `visible`、拖拽排序 `sort_order`）；
- **延迟初始化**：新用户首次获取分类时，系统自动将 `category` 模板克隆一份到 `user_category`；所有业务层增删改查仅面向 `user_category` 操作。

---

## 五、缓存与性能优化

- **交易流水缓存**：`TransactionServiceImpl.list()` 采用 `@Cacheable(value="transactions", key="T(...AuthUtils).getCurrentUserId() + '::' + #bookId + '::' + #month")`，带租户与月份复合键，TTL 30 分钟；
- **写操作精确淘汰**：新增、编辑、批量导入、删除时通过 `evictTransactionCache(bookId)` 程序化精准淘汰当前用户当月缓存，**严禁全局清空**；
- **分类协商缓存**：`GET /api/v1/categories` 接口计算并下发 `ETag` 响应头，前端携带 `If-None-Match`，分类无变化时直接返回 `304 Not Modified`，大幅减少移动端流量消耗。

---

## 六、安全与防御纵深

- **密码加密传输**：前端提交密码前调用 `crypto.ts` 使用 RSA 后端公钥加密，后端使用私钥解密后再通过 BCrypt 校验哈希；
- **Spring Security**：双 Token 认证机制，`JwtAuthFilter` 仅作为 `@Bean` 注入；未登录返回统一 401 触发前端无感换票；
- **输入净化 (XSS / SQL / CSV 注入)**：
  - MyBatis-Plus Lambda 表达式彻底规避 SQL 注入；
  - `SanitizeUtils.escapeHtml()` 对用户输入进行实体转义；
  - 导出 CSV 时对 `=`/`+`/`-`/`@` 特殊公式前缀自动加单引号规避注入风险；
- **接口速率限制 (Rate Limiting)**：基于 Redis 令牌桶计数器，对 `/auth/**` 高敏感接口实施 IP 级别频次限制。
