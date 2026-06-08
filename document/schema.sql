-- ============================================================
-- LedgerPro 一期数据库建表脚本 (MySQL 8.0)
-- 执行方式: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS ledger_pro DEFAULT CHARACTER SET utf8mb4;
USE ledger_pro;

-- 1. user
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(50) NOT NULL DEFAULT '',
  `avatar_url` VARCHAR(255) NOT NULL DEFAULT '',
  `phone` VARCHAR(20) NOT NULL DEFAULT '',
  `email` VARCHAR(100) NOT NULL DEFAULT '',
  `password_hash` VARCHAR(255) NOT NULL DEFAULT '',
  `data_key_salt` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. account_book
DROP TABLE IF EXISTS `account_book`;
CREATE TABLE `account_book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL DEFAULT '',
  `cover_icon` VARCHAR(255) NOT NULL DEFAULT '',
  `type` TINYINT NOT NULL DEFAULT 0,
  `owner_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. account
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `book_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL DEFAULT '',
  `type` TINYINT NOT NULL DEFAULT 0,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY',
  `icon` VARCHAR(255) NOT NULL DEFAULT '',
  `initial_balance` INT NOT NULL DEFAULT 0,
  `is_include_total` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. category
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `book_id` BIGINT DEFAULT NULL,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `name` VARCHAR(30) NOT NULL DEFAULT '',
  `type` TINYINT NOT NULL DEFAULT 0,
  `icon` VARCHAR(255) NOT NULL DEFAULT '',
  `color` VARCHAR(20) NOT NULL DEFAULT '',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_book_type` (`book_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. transaction
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `book_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL,
  `amount` INT NOT NULL,
  `fee` INT NOT NULL DEFAULT 0,
  `account_id` BIGINT NOT NULL,
  `to_account_id` BIGINT DEFAULT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `trans_time` DATETIME NOT NULL,
  `timezone` VARCHAR(40) NOT NULL DEFAULT 'Asia/Shanghai',
  `remark` VARCHAR(200) NOT NULL DEFAULT '',
  `merchant` VARCHAR(100) NOT NULL DEFAULT '',
  `location` VARCHAR(255) NOT NULL DEFAULT '',
  `image_urls` TEXT,
  `status` TINYINT NOT NULL DEFAULT 0,
  `periodic_rule_id` BIGINT DEFAULT NULL,
  `template_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_book_trans_time` (`book_id`, `trans_time`),
  KEY `idx_account` (`account_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预置支出分类 (type=0, ID 与前端一致)
INSERT INTO `category` (`id`, `book_id`, `parent_id`, `name`, `type`, `icon`, `color`, `sort_order`) VALUES
(1,  NULL, 0, '餐饮', 0, '🍔', '#ff6b35', 1),
(2,  NULL, 0, '交通', 0, '🚌', '#5b9bd5', 2),
(3,  NULL, 0, '购物', 0, '🛍', '#ff6b8a', 3),
(4,  NULL, 0, '娱乐', 0, '🎮', '#a29bfe', 4),
(5,  NULL, 0, '居家', 0, '🏠', '#fdcb6e', 5),
(6,  NULL, 0, '通讯', 0, '📱', '#00b894', 6),
(7,  NULL, 0, '医疗', 0, '💊', '#e17055', 7),
(8,  NULL, 0, '教育', 0, '📚', '#00cec9', 8),
(9,  NULL, 0, '人情', 0, '🎁', '#fd79a8', 9),
(10, NULL, 0, '其他', 0, '💸', '#636e72', 10);

-- 预置收入分类 (type=1)
INSERT INTO `category` (`id`, `book_id`, `parent_id`, `name`, `type`, `icon`, `color`, `sort_order`) VALUES
(101, NULL, 0, '工资', 1, '💰', '#00b894', 1),
(102, NULL, 0, '兼职', 1, '💼', '#5b9bd5', 2),
(103, NULL, 0, '理财', 1, '📈', '#fdcb6e', 3),
(104, NULL, 0, '红包', 1, '🧧', '#ff6b6b', 4),
(105, NULL, 0, '退款', 1, '↩️', '#00cec9', 5),
(106, NULL, 0, '其他', 1, '💵', '#636e72', 6);

ALTER TABLE `category` AUTO_INCREMENT = 1000;

-- 验证
SELECT '=== tables created ===' AS status;
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'ledger_pro' ORDER BY TABLE_NAME;
