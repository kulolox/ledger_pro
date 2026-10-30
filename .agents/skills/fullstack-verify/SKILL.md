---
name: fullstack-verify
description: 当完成功能开发、修复 Bug 或准备提交代码前，需要对前端和后端进行全量测试、类型检查和代码格式校验时使用。
---

# LedgerPro 全栈自动化验证流程

本技能用于一键执行前后端质量门禁，确保没有类型错误、单测挂掉或格式破损。

## 验证步骤

### 1. 后端验证 (Spring Boot)
在项目根目录或 `back_end` 目录下执行：
```bash
cd back_end

# 1. 编译语法与生成类检查
mvn test-compile

# 2. 运行所有单元测试
mvn test

# 3. 校验 Spotless 代码格式
mvn spotless:check
```
> 如果格式检查未通过，执行 `mvn spotless:apply` 进行自动格式化。

### 2. 前端验证 (uni-app + Vue 3 + TS)
在项目根目录或 `front_end` 目录下执行：
```bash
cd front_end

# 1. TypeScript 严格类型检查
pnpm type-check

# 2. Vitest 单元测试
pnpm test:unit
```

### 3. 可选：端到端回归测试 (E2E)
若修改涉及核心登录或记账主流程，启动本地服务后执行：
```bash
cd front_end
pnpm test:e2e
```
