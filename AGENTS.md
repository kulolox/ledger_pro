# LedgerPro — Antigravity Agent 指南与全局规范

本项目为多账本个人记账应用 **LedgerPro**。
- **前端**：`front_end/`（uni-app 3.0 + Vue 3 Composition API + TypeScript + Vite 5 + Wot UI）
- **后端**：`back_end/`（Spring Boot + Java 21 + MyBatis-Plus 3.5 + Spring Security + MySQL 8 + Redis 7）
- **文档与过程文件**：`document/`（需求 PRD、设计架构、API 契约、数据库脚本）

---

## 0. 最高执行红线与交互纪律（用户明确提出，最高执行优先级）

> [!CAUTION]
> 以下三项为用户明确提出的**绝对红线**，任何时候其优先级均高于自主决策，无任何例外：

### 1. 严禁擅自执行 `git commit` 或 `git push`（修改审核与提交红线）
* **绝对禁止自主提交或推送**：任何代码、文档或脚本修改完成后，**严禁私自执行 `git commit`、`git push` 或 `git tag`**。
* **强制汇报机制**：完成任何文件改动后，**必须先清晰、准确地向用户汇报改动的文件列表及具体的代码变更内容**。
* **等待授权**：只有在用户明确审核确认并给出明确提交指令（如“提交代码”、“推送分支”）后，方可执行对应操作。

### 2. 严禁擅自执行全量验证（验证范围控制红线）
* **日常开发/Bug修复严禁跑全量**：
  - 严禁擅自执行 `pnpm test:unit`（前端全部 110+ 单测）；
  - 严禁擅自执行 `mvn test`（后端全部 100+ 单测）；
  - 严禁擅自执行 Playwright 全量 E2E 测试或全栈全量验证脚本。
* **改哪测哪，最小作用域验证原则**：
  - 日常改动仅允许针对改动文件做最小范围必要验证（如前端执行 `pnpm type-check` 类型检查，或针对性单文件测试 `npx vitest run src/xxx/__tests__/xxx.test.ts`；后端执行 `mvn test-compile` 或单个测试类 `mvn test -Dtest=XxxTest`）。
* **全量验证唯一例外**：**只有在用户明确给出特别文本指令（如“走全量验证”、“跑全量测试”）时，才允许执行全量测试套件**。

### 3. 私有发包与运维工作流绝对解耦（不开源红线）
* **工作流私有化**：自动化发包脚本（`scripts/`）、构建脚本（`release.bat`）、生产服务器运维部署与环境变量文件（`.env`）均属于私有工作流，**绝不开源**。
* **防泄露隔离**：上述私有文件必须严格被 `.gitignore` 保护，**严禁将其跟踪进 Git 仓库或推送到公开仓库**。

---

## 1. 核心业务与架构强约束

1. **金额存储与传输**：
   * 数据库与后端一律使用 `INT` 存储，单位为**“分”**。
   * 前后端传输遵循：展示元 / 存储分（前端输入元 $\times 100$ 传给后端；展示时 $\div 100$）。
2. **多账本数据隔离**：
   * 所有交易记录通过 `book_id` 进行硬隔离。
   * 所有的交易查询、新增、修改、删除操作**必须严格校验 `book_id` 归属当前登录用户**。
   * `book_id` 为雪花算法/随机 8-19 位大整数。
3. **雪花算法 ID 强约束（严禁数值类型转换，最高红线）**：
   * 数据库所有实体主键与外键 ID（`user.id`, `account_book.id`, `account.id`, `user_category.id`, `transaction.id`, `book_id` 等）均为 64 位 Long 整数（雪花算法 ID，通常 19 位）。
   * **后端强约束**：通过 HTTP JSON 接口向前端传输时，**所有 Long 类型的 ID 必须全局使用 `ToStringSerializer` 序列化为纯字符串**，严禁以原生 JSON 数字输出。
   * **前端强约束（雪花 ID 不得转换）**：
     * 前端所有实体 ID 的 TS 类型一律声明为 `string`，**严禁使用 `number`**。
     * **严禁对任何实体 ID 执行数值类型转换**：严禁使用 `Number(id)`、`parseInt(id)`、`parseFloat(id)`、`+id` 或盲目 `JSON.parse(id)`，防止超过 JavaScript 安全整数范围（IEEE 754 53位截断，末位被抹平为00）。
     * **前端 ID 比对一律做字符串归一化**：比对两个 ID 是否相等时，必须统一使用 `String(a) === String(b)`，严禁裸用 `a === b` 防止隐式类型不一致导致未命中。
     * **Storage 存取雪花 ID 严禁走 `JSON.parse`**：针对纯字符串 ID 存取必须保持原生字符串读写。
4. **逻辑删除**：
   * 所有核心表均使用 `deleted` 字段进行逻辑删除，由 MyBatis-Plus 自动过滤。
5. **统一响应协议**：
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

---

## 2. 文档索引与过程文件导航（先查文档，按需加载）

所有业务与架构设计均位于 `document/` 目录，遇到相关需求时**优先查阅对应文档**，避免臆测：

* **产品需求与 PRD**：[document/specs/产品需求.md](document/specs/产品需求.md)
* **技术架构与运维配置**：[document/design/architecture.md](document/design/architecture.md)（含 Docker、Nginx、Spring Security、Redis 缓存与邮件配置）
* **接口详细契约**：[document/specs/API接口清单.md](document/specs/API接口清单.md)
* **数据库表结构 (DDL)**：[document/specs/schema.sql](document/specs/schema.sql)
* **Wot UI 组件库用法与知识库**：[document/reference/wotui-knowledge.md](document/reference/wotui-knowledge.md) 与 [document/reference/wotui-data.json](document/reference/wotui-data.json)

---

## 3. 子目录工程规范指引

* **前端专属规则**：见 [front_end/AGENTS.md](front_end/AGENTS.md)
* **后端专属规则**：见 [back_end/AGENTS.md](back_end/AGENTS.md)
* **专属技能 (Skills)**：位于 `.agents/skills/`（Playwright E2E 测试、Wot UI 组件速查、全栈测试验证等）

---

## 4. 工程验证与测试守则（明确区分日常精准验证 vs 用户指令全量验证）

### A. 日常开发/修 Bug 允许且推荐的精准检查（按需选用）
* **前端类型安全检查**：`cd front_end && pnpm type-check`
* **前端单个测试文件运行**：`cd front_end && npx vitest run src/xxx/__tests__/xxx.test.ts`
* **后端编译与语法校验**：`cd back_end && mvn test-compile`
* **后端单个测试类运行**：`cd back_end && mvn test -Dtest=XxxTest`
* **后端代码格式化**：`cd back_end && mvn spotless:apply`

### B. 严禁擅自执行的全量套件（只有用户明确要求时方可运行）
> [!WARNING]
> 以下命令只有在用户在会话中明确输入“跑全量测试”、“走全量验证”等显式指令时才允许执行：
* ❌ 前端全量单测：`cd front_end && pnpm test:unit`
* ❌ 后端全量测试：`cd back_end && mvn test`
* ❌ 全栈一键测试：激活技能 `.agents/skills/ledger-verify` 或 `fullstack-verify`
