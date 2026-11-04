# LedgerPro 项目核心文档体系 (`document/`)

本项目已进入成熟稳定运行阶段，所有历史过程性记录、过期技术方案与已作废迭代计划均已整理清理。核心业务、架构、接口与数据库技术资产集中收纳如下：

---

## 核心文档快速导航

- 📘 **产品需求与功能规格** → [specs/产品需求.md](./specs/产品需求.md)
- 🏗️ **全栈架构与部署设计** → [design/architecture.md](./design/architecture.md)
- 🔌 **RESTful API 接口契约** → [specs/API接口清单.md](./specs/API接口清单.md)
- 🗄️ **数据库表结构设计 (DDL)** → [specs/schema.sql](./specs/schema.sql)
- 🎨 **Wot UI 组件知识库** → [reference/wotui-knowledge.md](./reference/wotui-knowledge.md)

---

## 目录结构说明

```
document/
├── specs/       # 业务与系统规格（PRD、RESTful API 契约、MySQL DDL）
├── design/      # 技术架构方案（Docker 部署、离线同步、安全防护、多账本隔离）
├── reference/   # 第三方组件库知识库（Wot UI 规范数据）
├── images/      # 系统核心展示截图
└── README.md    # 文档导航中心（当前文档）
```

---

## 文档明细清单

### 1. `specs/` —— 业务与技术规格
| 文件 | 类型 | 内容说明 | 维护状态 |
| :--- | :--- | :--- | :--- |
| **`产品需求.md`** | Markdown | 产品定义、目标用户、核心功能阵型与页面流转定义 | 核心资产 |
| **`API接口清单.md`** | Markdown | 认证、账本、分类、记账流水、查询、导入导出全量 REST 契约 | 核心资产 |
| **`schema.sql`** | SQL | MySQL 8.0 完整建表脚本（含 utf8mb4、TINYINT、多租户硬隔离索引） | 核心资产 |

### 2. `design/` —— 架构与设计
| 文件 | 类型 | 内容说明 | 维护状态 |
| :--- | :--- | :--- | :--- |
| **`architecture.md`** | Markdown | 4 容器部署、本地优先离线同步状态机、Spring Security、Redis 租户缓存与安全防注入 | 核心资产 |

### 3. `reference/` —— 参考与知识库
| 文件 | 类型 | 内容说明 | 维护状态 |
| :--- | :--- | :--- | :--- |
| **`wotui-knowledge.md`** | Markdown | Wot UI 组件属性、事件、插槽与移动端避坑用法 | 长期维护 |
| **`wotui-data.json`** | JSON | Wot UI 组件库元数据结构化定义 | 长期维护 |
