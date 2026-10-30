# LedgerPro 前端工程规范 (`front_end`)

本项目为基于 **uni-app 3.0 + Vue 3 (Composition API) + TypeScript + Vite 5** 的跨端记账前端。

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

## 4. 自动化测试与质量门禁

* **TypeScript 类型检查**：`pnpm type-check`（提交或交付前必须通过，严禁使用 `any` 绕过校验）。
* **单元测试**：`pnpm test:unit`（Vitest）。
* **端到端测试 (E2E)**：`pnpm test:e2e`（Playwright 驱动，Page Object 架构，视口 390×844）。
* **开发命令**：
  ```bash
  pnpm dev:h5      # 启动 H5 开发调试
  pnpm build:h5    # 构建打包 H5
  ```
