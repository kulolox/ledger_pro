# Changelog

## [Unreleased]

## [0.5.0] - 2026-06-23

### Added
- 导入导出功能（CSV/Excel/JSON），支持按年导出
- 导入分类不匹配自动归入"其他"
- E2E Playwright 测试全覆盖（登录/注册/首页/分类/统计/侧边栏/导入导出）

### Fixed
- Docker 部署编码修复（MySQL latin1 / Alpine JVM / nginx charset / Spring encoding）
- Spring Security 认证链路修复 + RSA 密钥持久化
- 项目审查报告 19 项安全/代码缺陷修复
- JSON 导入空字符串解析
- Excel 导出/模板下载后缀修正

### Changed
- JPA → MyBatis-Plus 迁移
- Service 层接口/实现分离
- 分类从客户端模拟迁移到服务端双表设计
- 账户数据按 `bookId` 隔离

## [0.4.0] - 2026-06-11

### Added
- 分类管理页面（服务端 + user_category）
- RSA 密码加密传输
- Redis 缓存（@Cacheable + userId 隔离 + 精确逐出）
- JWT 双 token 机制（access 24h + refresh 7d）

### Fixed
- Redis 序列化配置修复
- 注册无重复校验

## [0.3.0] - 2026-06-09

### Added
- 统计模块（月度收支汇总、分类饼图、ECharts）
- 顶部导航栏重构
- 后端分层完善（Controller → Service → Mapper）

## [0.1.0] - 2026-06-08

### Added
- 项目初始化（uni-app 前端 + Spring Boot 后端）
- 用户注册/登录（BCrypt + JWT）
- 核心记账循环（增删改查交易、多账本支持）
- 基础 UI（首页记账、侧边栏、分类选择）
