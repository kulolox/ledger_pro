# LedgerPro RESTful API 接口规范

- **版本前缀**：`/api/v1`
- **协议响应**：HTTP 状态码固定返回 200，包体采用统一 JSON 格式：
  - 成功：`{ "code": 0, "message": "success", "data": ... }`
  - 业务失败：`{ "code": 400xxx, "message": "错误提示", "data": null }`
- **金额单位**：接口传输一律使用**“分”**（Integer），前端负责 `× 100` / `÷ 100` 的元分转换。
- **认证方式**：基于双 Token（Access Token 24h + Refresh Token 7d），除白名单接口外，请求头均须携带：
  `Authorization: Bearer <access_token>`

---

## 接口总览速查

| 模块 | 方法 | 接口路径 | 说明 | 鉴权 |
| :--- | :--- | :--- | :--- | :--- |
| **认证模块** | POST | `/api/v1/auth/register` | 用户注册（RSA 密码传输） | 无需 |
| | POST | `/api/v1/auth/login` | 用户登录（返回 Access/Refresh Token） | 无需 |
| | POST | `/api/v1/auth/refresh` | 刷新 Access Token | 需 RefreshToken |
| | POST | `/api/v1/auth/send-reset-code` | 发送找回密码邮件验证码 | 无需 |
| | POST | `/api/v1/auth/reset-password` | 验证码重置密码 | 无需 |
| | POST | `/api/v1/auth/change-password` | 已登录修改密码 | 需登录 |
| **用户模块** | GET | `/api/v1/user/info` | 获取当前登录用户画像 | 需登录 |
| **账本模块** | GET | `/api/v1/account-books` | 获取当前用户的账本列表 | 需登录 |
| | POST | `/api/v1/account-books` | 创建新账本 | 需登录 |
| | PUT | `/api/v1/account-books/{id}` | 更新账本信息 | 需登录 |
| | DELETE | `/api/v1/account-books/{id}` | 软删除账本 | 需登录 |
| **分类模块** | GET | `/api/v1/categories` | 获取收支分类树（支持 ETag 304 缓存） | 需登录 |
| | POST | `/api/v1/categories` | 新增自定义分类 | 需登录 |
| | PUT | `/api/v1/categories/{id}` | 修改分类（名称/图标/颜色/显隐） | 需登录 |
| | DELETE | `/api/v1/categories/{id}` | 删除分类（支持级联迁移账单或级联删除） | 需登录 |
| | PUT | `/api/v1/categories/reorder` | 批量更新分类排序 | 需登录 |
| **交易记账** | GET | `/api/v1/transactions` | 按账本+月份分页获取账单流水（带 Redis 缓存） | 需登录 |
| | POST | `/api/v1/transactions` | 单笔记账（自动淘汰缓存） | 需登录 |
| | POST | `/api/v1/transactions/batch` | 批量同步记账（离线恢复场景） | 需登录 |
| | PUT | `/api/v1/transactions/{id}` | 修改单笔流水 | 需登录 |
| | DELETE | `/api/v1/transactions/{id}` | 软删除单笔流水 | 需登录 |
| | POST | `/api/v1/transactions/batch-delete` | 批量软删除流水 | 需登录 |
| | GET | `/api/v1/transactions/search` | 多维度复合查询（关键字/类型/分类/时间段/账本） | 需登录 |
| | GET | `/api/v1/transactions/statistics` | 收支统计与分类汇总占比报表 | 需登录 |
| **导入导出** | GET | `/api/v1/transactions/export` | 导出账单文件（支持 CSV / Excel / JSON） | 需登录 |
| | POST | `/api/v1/transactions/import` | 导入账单流水（Multipart 文件解析） | 需登录 |
| **系统运维** | GET | `/api/v1/health` | 容器健康探针 | 无需 |
| | GET | `/api/v1/system/version/check` | 检查客户端最新版本与国内加速下载直链 | 无需 |

---

## 核心接口契约详解

### 1. 认证鉴权

#### 1.1 用户登录
- **请求**：`POST /api/v1/auth/login`
- **请求体**：
  ```json
  {
    "email": "user@example.com",
    "password": "<RSA_ENCRYPTED_BASE64>"
  }
  ```
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "access_token": "eyJhbGciOi...",
      "refresh_token": "eyJhbGciOi...",
      "expires_in": 86400,
      "user_info": {
        "id": 1,
        "email": "user@example.com",
        "nickname": "LedgerUser",
        "avatar_url": ""
      }
    }
  }
  ```

#### 1.2 刷新 Token
- **请求**：`POST /api/v1/auth/refresh`
- **请求头**：`Authorization: Bearer <refresh_token>`
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "access_token": "eyJhbGciOi...",
      "expires_in": 86400
    }
  }
  ```

---

### 2. 交易流水

#### 2.1 获取流水列表（按月）
- **请求**：`GET /api/v1/transactions?book_id=12345678&month=2026-11`
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "month": "2026-11",
      "total_income": 800000,
      "total_expense": 250000,
      "list": [
        {
          "id": "1001",
          "book_id": "12345678",
          "type": 0,
          "category_id": "1",
          "amount": 2500,
          "remark": "午餐",
          "trans_time": "2026-11-04 12:30:00",
          "is_include_total": 1
        }
      ]
    }
  }
  ```

#### 2.2 账单多维复合查询
- **请求**：`GET /api/v1/transactions/search`
- **查询参数**：
  - `book_id`（可选）：指定账本；若为空则在用户所有有效账本内查询；
  - `keyword`（可选）：备注模糊搜索；
  - `type`（可选）：0 支出，1 收入；
  - `category_id`（可选）：分类 ID；
  - `date_from`、`date_to`（可选）：时间区间（`yyyy-MM-dd`）；
  - `page`、`size`：分页参数（默认 page=1, size=20）。
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "total": 35,
      "page": 1,
      "size": 20,
      "list": [...]
    }
  }
  ```

#### 2.3 批量创建流水（离线推送同步）
- **请求**：`POST /api/v1/transactions/batch`
- **请求体**：
  ```json
  {
    "items": [
      {
        "book_id": "12345678",
        "type": 0,
        "category_id": "1",
        "amount": 3500,
        "trans_time": "2026-11-04 14:00:00",
        "remark": "咖啡"
      }
    ]
  }
  ```

---

### 3. 分类管理

#### 3.1 获取分类树
- **请求**：`GET /api/v1/categories`
- **缓存策略**：支持 HTTP 304（客户端携带 `If-None-Match: <etag>`，分类未发生变动时直接返回 304 Not Modified）。
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": [
      {
        "id": 1,
        "name": "餐饮",
        "type": 0,
        "icon": "🍔",
        "color": "#ff6b35",
        "sort_order": 1,
        "visible": 1,
        "is_system": 1
      }
    ]
  }
  ```

#### 3.2 删除分类
- **请求**：`DELETE /api/v1/categories/{id}?targetCategoryId=10&deleteTransactions=false`
- **参数说明**：
  - `targetCategoryId`（可选）：若该分类下存在已有关联账单，可指定级联迁移至目标分类 ID；
  - `deleteTransactions`（可选）：布尔值，是否连同分类下的所有流水一同级联删除。

---

### 4. 系统版本与升级

#### 4.1 检查最新版本
- **请求**：`GET /api/v1/system/version/check`
- **成功响应**：
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "latestVersion": "1.0.3",
      "versionCode": 103,
      "minVersion": "1.0.0",
      "downloadUrl": "https://ghfast.top/https://github.com/kulolox/ledger_pro/releases/download/v1.0.3/LedgerPro-v1.0.3-prod-release-20261104.apk",
      "changelog": "1. 修复已知异常，优化体验",
      "forceUpdate": false
    }
  }
  ```
