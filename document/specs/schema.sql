-- ============================================================
-- LedgerPro 数据库建表脚本 (MySQL 8.0)
-- 字符集: utf8mb4 / 排序规则: utf8mb4_unicode_ci
-- 执行方式: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS ledger_pro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ledger_pro;
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 用户表 (user)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户昵称',
  `avatar_url` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '手机号',
  `email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱（登录账号）',
  `password_hash` VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'BCrypt密码哈希',
  `reset_code` VARCHAR(10) DEFAULT NULL COMMENT '密码重置验证码',
  `reset_code_expires_at` DATETIME DEFAULT NULL COMMENT '验证码过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础表';

-- ------------------------------------------------------------
-- 2. 账本表 (account_book)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `account_book`;
CREATE TABLE `account_book` (
  `id` BIGINT NOT NULL COMMENT '账本ID (雪花算法/随机8-12位)',
  `name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '账本名称',
  `cover_icon` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图标',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '账本类型: 0默认 1家庭 2生意等',
  `owner_id` BIGINT NOT NULL COMMENT '归属用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';

-- ------------------------------------------------------------
-- 3. 账户表 (account)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `book_id` BIGINT NOT NULL COMMENT '所属账本ID',
  `name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '账户名称 (现金/微信/支付宝等)',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '账户类别: 0普通 1信用卡 2投资',
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '货币代码',
  `icon` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '账户图标',
  `initial_balance` INT NOT NULL DEFAULT 0 COMMENT '初始余额 (单位: 分)',
  `is_include_total` TINYINT NOT NULL DEFAULT 1 COMMENT '是否计入总资产: 1是 0否',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产账户表';

-- ------------------------------------------------------------
-- 4. 系统分类模板表 (category) - 全局只读模板
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板分类ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `name` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分类名称',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '分类类型: 0支出 1收入',
  `icon` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '图标或Emoji',
  `color` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '主题颜色色值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `is_system` TINYINT NOT NULL DEFAULT 1 COMMENT '是否系统模板: 固定为1',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统分类模板表';

-- 预置基础分类模板 (10 支出 + 6 收入)
INSERT INTO `category` (`id`, `parent_id`, `name`, `type`, `icon`, `color`, `sort_order`, `is_system`) VALUES
(1,  0, '餐饮', 0, '🍔', '#ff6b35', 1, 1),
(2,  0, '交通', 0, '🚌', '#5b9bd5', 2, 1),
(3,  0, '购物', 0, '🛍', '#ff6b8a', 3, 1),
(4,  0, '娱乐', 0, '🎮', '#a29bfe', 4, 1),
(5,  0, '居家', 0, '🏠', '#fdcb6e', 5, 1),
(6,  0, '通讯', 0, '📱', '#00b894', 6, 1),
(7,  0, '医疗', 0, '💊', '#e17055', 7, 1),
(8,  0, '教育', 0, '📚', '#00cec9', 8, 1),
(9,  0, '人情', 0, '🎁', '#fd79a8', 9, 1),
(10, 0, '其他', 0, '💸', '#636e72', 10, 1),
(101, 0, '工资', 1, '💰', '#00b894', 1, 1),
(102, 0, '兼职', 1, '💼', '#5b9bd5', 2, 1),
(103, 0, '理财', 1, '📈', '#fdcb6e', 3, 1),
(104, 0, '红包', 1, '🧧', '#ff6b6b', 4, 1),
(105, 0, '退款', 1, '↩️', '#00cec9', 5, 1),
(106, 0, '其他', 1, '💵', '#636e72', 6, 1);

-- ------------------------------------------------------------
-- 5. 用户个性化分类表 (user_category) - 租户隔离
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user_category`;
CREATE TABLE `user_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
  `user_id` BIGINT NOT NULL COMMENT '归属用户ID',
  `source_id` BIGINT DEFAULT NULL COMMENT '来源模板ID (null表示用户完全自定义)',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `name` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分类名称',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型: 0支出 1收入',
  `icon` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '图标或Emoji',
  `color` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '颜色十六进制值',
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否源自系统预设: 1是 0否',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见: 1可见 0隐藏',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '展示排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_id`, `type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户独立分类表';

-- ------------------------------------------------------------
-- 6. 记账交易明细表 (transaction)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `book_id` BIGINT NOT NULL COMMENT '所属账本ID (租户物理隔离键)',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '交易类型: 0支出 1收入 2转账',
  `amount` INT NOT NULL COMMENT '交易金额 (单位: 分，正整数)',
  `account_id` BIGINT NOT NULL COMMENT '支出/转出资产账户ID',
  `to_account_id` BIGINT DEFAULT NULL COMMENT '转入资产账户ID (转账时必填)',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID (对应 user_category.id)',
  `trans_time` DATETIME NOT NULL COMMENT '交易发生时间 (精确到秒)',
  `remark` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '交易备注说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录录入时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删 1已删',
  PRIMARY KEY (`id`),
  -- 核心组合索引：账本隔离 + 软删除 + 时间范围聚合查询
  KEY `idx_book_del_time` (`book_id`, `deleted`, `trans_time`),
  -- 账户与分类筛选索引：用于资产分析与分类报表加速
  KEY `idx_account_id` (`account_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记账交易流水表';

-- ------------------------------------------------------------
-- 7. Token 黑名单表 (token_blacklist) - JWT 注销与登出
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `token_blacklist`;
CREATE TABLE `token_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `token_hash` VARCHAR(64) NOT NULL COMMENT 'JWT SHA-256摘要哈希',
  `expires_at` DATETIME NOT NULL COMMENT 'Token原本过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入黑名单时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token黑名单失效表';
