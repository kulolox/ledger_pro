# LedgerPro — Antigravity Agent 指南与全局规范

本项目为多账本个人记账应用 **LedgerPro**。
- **前端**：`front_end/`（uni-app 3.0 + Vue 3 Composition API + TypeScript + Vite 5 + Wot UI）
- **后端**：`back_end/`（Spring Boot + Java 21 + MyBatis-Plus 3.5 + Spring Security + MySQL 8 + Redis 7）
- **文档与过程文件**：`document/`（需求 PRD、设计架构、API 契约、数据库脚本）

---

## 1. 核心业务与架构强约束（红线）

1. **金额存储与传输**：
   * 数据库与后端一律使用 `INT` 存储，单位为**“分”**。
   * 前后端传输遵循：展示元 / 存储分（前端输入元 $\times 100$ 传给后端；展示时 $\div 100$）。
2. **多账本数据隔离**：
   * 所有交易记录通过 `book_id` 进行硬隔离。
   * 所有的交易查询、新增、修改、删除操作**必须严格校验 `book_id` 归属当前登录用户**。
   * `book_id` 为随机 8-12 位数字。
3. **逻辑删除**：
   * 所有核心表均使用 `deleted` 字段进行逻辑删除，由 MyBatis-Plus 自动过滤。
4. **统一响应协议**：
   * 后端统一返回 HTTP 200，包体内包含标准 JSON 结构：
     `Result.success(data)` 或 `Result.fail(code, message)`。
   * 前端 `request.ts` 统一拦截：
     * HTTP $\ge 300$ 走网络状态码异常处理；
     * HTTP 200 但 `code !== 0` 走业务异常（弹出 Toast 提示 `message`）。
5. **安全与加解密**：
   * 用户密码传输：前端使用 RSA 公钥加密 $\rightarrow$ 后端私钥解密 $\rightarrow$ BCrypt Hash 校验/入库。
   * 双 Token 认证：Access Token（有效期 24h）+ Refresh Token（有效期 7d），通过 `type` claim 区分。
   * 401 自动无感刷新：前端捕获 401 后自动调用 `/api/v1/auth/refresh` 换取新 Token 并无感重试。
6. **数据库设计规范（MySQL 8.0）**：
   * 字符集统一 `utf8mb4`。
   * 状态与布尔字段（如 `deleted`, `type`, `visible`）统一使用 `TINYINT`，严禁使用 4 字节的 `INT`。
   * 字段强制 `NOT NULL` 并赋默认值，禁止随意留 `NULL`。
   * 核心流水表建立 `(book_id, deleted, trans_time)` 覆盖复合索引；`account_id`、`category_id` 必须建立索引。
   * 具备唯一性特征的字段（如 `email`）必须建立物理 `UNIQUE KEY` 防并发重复。
7. **修改审核与提交红线（核心纪律）**：
   * **严禁擅自执行 `git commit` 或 `git push`**。
   * 任何代码或文件修改完成后，**必须先清晰向用户汇报改动的文件及具体变更内容**，等待用户审核通过并给出明确指令后，方可执行提交。

---

## 2. 文档索引与过程文件导航（先查文档，按需加载）

所有业务与架构设计均位于 `document/` 目录，遇到相关需求时**优先查阅对应文档**，避免臆测：

* **产品需求与 PRD**：[document/specs/产品需求.md](document/specs/产品需求.md)
* **技术架构与运维配置**：[document/design/architecture.md](document/design/architecture.md)（含 Docker、Nginx、Spring Security、Redis 缓存与邮件配置）
* **接口详细契约**：[document/specs/一期接口清单.md](document/specs/一期接口清单.md)
* **数据库表结构 (DDL)**：[document/specs/schema.sql](document/specs/schema.sql)
* **Wot UI 组件库用法与知识库**：[document/reference/wotui-knowledge.md](document/reference/wotui-knowledge.md) 与 [document/reference/wotui-data.json](document/reference/wotui-data.json)

---

## 3. 子目录工程规范指引

* **前端专属规则**：见 [front_end/AGENTS.md](front_end/AGENTS.md)
* **后端专属规则**：见 [back_end/AGENTS.md](back_end/AGENTS.md)
* **专属技能 (Skills)**：位于 `.agents/skills/`（Playwright E2E 测试、Wot UI 组件速查、全栈测试验证等）

---

## 4. 常用工程验证命令

* **全栈一键测试**：激活技能 `.agents/skills/ledger-verify`
* **前端类型检查**：`cd front_end && pnpm type-check`
* **前端单元测试**：`cd front_end && pnpm test:unit`
* **后端单元测试**：`cd back_end && mvn test`
* **后端代码格式化**：`cd back_end && mvn spotless:apply`
