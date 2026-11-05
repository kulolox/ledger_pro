# LedgerPro 前端工程规范 (`front_end`)

本项目为基于 **uni-app 3.0 + Vue 3 (Composition API) + TypeScript + Vite 5** 的跨端记账前端。

---

## 0. 用户核心红线与纪律（最高执行优先级）

> [!CAUTION]
> 1. **严禁擅自 `git commit` 或 `git push`**：任何前端改动完成后必须先向用户清晰汇报变更文件与内容，等待用户审核通过并给出明确指令后方可提交。
> 2. **严禁擅自执行全量单元测试与 E2E 测试**：
>    - 严禁擅自运行 `pnpm test:unit`（全量 110+ 单测）与 `pnpm test:e2e`。
>    - 日常功能开发或 Bug 修复，仅允许执行针对性单用例测试（`npx vitest run src/path/to/test.ts`）或类型检查（`pnpm type-check`）。
>    - **只有在用户明确给出“跑全量测试”指令时才允许全量运行**。

---

## 1. 核心技术栈与架构

* **UI 组件库**：Wot UI (`@wot-ui/ui`)，组件统一以 `wd-` 开头（如 `<wd-button>`, `<wd-toast>`, `<wd-popup>`）。
  * **原则**：**优先使用 Wot UI 原生组件与插槽，严禁重复造轮子**。
  * 遇到组件参数或用法疑问，查阅技能 `wot-ui-guide` 或参考 [document/reference/wotui-knowledge.md](../document/reference/wotui-knowledge.md)。
* **样式规范**：
  * 使用 SCSS 嵌套语法书写样式。
  * **严禁颜色硬编码**，必须复用设计系统或主题定义的变量/类名。
* **路径与模块**：
  * `@/` 统一映射到 `src/` 目录。
  * 纯函数、算法及公共工具必须抽取到 `src/utils/`。
  * 公共数据模型与接口 Interface 统一定义在 `src/types/`。

---

## 2. 网络请求与安全规范

* **请求工具封装**：统一使用 `src/utils/request.ts` 中的 `request` 实例进行 HTTP 请求，严禁在页面或组件内裸调 `uni.request` 或 `fetch`。
* **认证鉴权**：
  * 请求拦截器已自动挂载 `Authorization: Bearer <access_token>`。
  * 401 状态码由拦截器统一捕获并执行无感换票（`tryRefreshToken()`）。
  * 特殊场景注意：若个别 Store 存在离线或特殊加载逻辑（如 `categoryStore`），须确保手动从 storage 获取 token 补充到 header。
* **密码传输加密**：
  * 登录与注册提交密码前，必须调用 `src/utils/crypto.ts` 使用 RSA 后端公钥加密后再发起传输。

---

## 3. 金额与同步规范

* **金额显示与输入**：
  * 页面输入元，提交给后端接口时必须转为分（`Math.round(amount * 100)`）。
  * 接口返回的金额均为分，渲染展示时必须除以 100 格式化为两位小数。
* **本地优先同步策略**：
  * 执行顺序：`pushDeletes` $\rightarrow$ `pushPending` $\rightarrow$ `pullFromServer`。
  * 必须使用 `syncing` 互斥锁防止并发同步引发冲突；同步失败不得污染破坏本地现有缓存。

---

## 4. 雪花算法 ID 强约束（雪花 ID 不得转换）

> [!CAUTION]
> 数据库所有表主键与外键 ID（`id`, `book_id`, `user_id`, `account_id`, `category_id` 等）均为 64 位大整数（雪花算法，通常 19 位），超过了 JavaScript 的 `Number.MAX_SAFE_INTEGER`（9007199254740991，16位）。任何转数值操作均会导致低位被精度抹平为 00，造成致命业务 Bug。

* **TS 类型严格收敛**：所有模型与 DTO 中的 ID 字段一律声明为 `string`，严禁定义为 `number`。
* **严禁主动数值转换**：严禁对任何 ID 字段使用 `Number(id)`、`parseInt(id)`、`parseFloat(id)` 或 `+id`。
* **Storage 存取雪花 ID 严禁走 `JSON.parse`**：针对账本 ID、流水 ID 等纯字符串 ID，存取必须直接调用原生字符串 API，通用 Storage 读取方法严禁将纯数字大整数字符串反序列化为 JS Number。
* **ID 比对一律做字符串归一化**：比对两个实体 ID 是否相等时（如高亮、勾选、查找），必须一律使用 `String(a) === String(b)`，严禁直接裸用 `a === b`，防止类型不一致导致未命中。
* **UI 组件传参防污染**：Picker / 下拉选择器 / 列表等组件的 `value` 字段必须使用 `string` 绑定，回调时必须保持 `String(val)` 取用。

---

## 5. 质量验证守则

* **日常类型检查**：`pnpm type-check`（提交或交付前必须通过，严禁使用 `any` 绕过校验）。
* **针对性单测**：`npx vitest run src/.../foo.test.ts`（只测改动的模块）。
* **全量单测（需用户授权）**：`pnpm test:unit`（严禁擅自执行）。
* **开发命令**：
  ```bash
  pnpm dev:h5      # 启动 H5 开发调试
  pnpm build:h5    # 构建打包 H5
  ```
