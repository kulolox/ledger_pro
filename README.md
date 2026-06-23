# LedgerPro

个人记账软件。支持多账本、分类管理、导入导出、统计图表。

## 技术栈

| 层     | 技术                                                  |
| ------ | ----------------------------------------------------- |
| 前端   | uni-app (Vue 3 + TypeScript + Vite 5) + Wot UI        |
| 后端   | Spring Boot 4.0 + Java 21 + MyBatis-Plus 3.5          |
| 数据库 | MySQL 8.0 + Redis 7                                   |
| 认证   | JWT 双 token (access 24h + refresh 7d) + RSA 密码加密 |
| 部署   | Docker Compose (nginx + backend + mysql + redis)      |

## 项目结构

```
ledger_pro/
├── document/          # 产品需求、技术方案、迭代计划
├── front_end/         # uni-app 前端工程
└── back_end/          # Spring Boot 后端工程
```

## 快速开始

### 前端

```bash
cd front_end
pnpm install
cp .env.example .env      # 编辑配置
pnpm dev:h5               # 启动 H5 开发服务器 (http://localhost:5173)
```

### 后端

```bash
cd back_end
cp .env.example .env       # 编辑 DB/JWT/邮件 配置
mvn spring-boot:run        # 启动开发服务器 (http://localhost:8080)
```

## Docker 部署

```bash
cd back_end
mvn clean package -DskipTests
docker-compose build --no-cache backend
docker-compose up -d
```

4 个容器：nginx (80) → backend (8080) + mysql + redis。数据挂载到 `D:/data_center/prod/`。

## 测试

```bash
# 前端单元测试
cd front_end && pnpm test:unit

# 前端 E2E 测试（需要后端容器 + 前端 dev server 运行中）
pnpm test:e2e
pnpm test:e2e -- --grep "Auth"

# 后端测试
cd back_end && mvn test
```

## 更多

详细架构设计、安全策略、同步机制见 [CLAUDE.md](./CLAUDE.md)。

## 页面预览

![](/document/image/Snipaste_2026-06-23_10-44-06.png)

![](/document/image/Snipaste_2026-06-23_10-44-41.png)

![](/document/image/Snipaste_2026-06-23_10-44-53.png)

![](/document/image/Snipaste_2026-06-23_10-45-02.png)

![](/document/image/Snipaste_2026-06-23_10-45-15.png)

![](/document/image/Snipaste_2026-06-23_10-45-45.png)
