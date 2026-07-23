SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nickname` VARCHAR(255) DEFAULT '',
    `avatar_url` VARCHAR(500) DEFAULT '',
    `phone` VARCHAR(20) DEFAULT '',
    `email` VARCHAR(255) DEFAULT '',
    `password_hash` VARCHAR(255) DEFAULT '',
    `data_key_salt` VARCHAR(255) DEFAULT NULL,
    `reset_code` VARCHAR(10) DEFAULT NULL,
    `reset_code_expires_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_email` (`email`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `account_book` (
    `id` BIGINT NOT NULL,
    `name` VARCHAR(255) DEFAULT '',
    `cover_icon` VARCHAR(255) DEFAULT '',
    `type` INT DEFAULT 0,
    `owner_id` BIGINT DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(255) DEFAULT '',
    `type` INT DEFAULT 0,
    `currency` VARCHAR(10) DEFAULT 'CNY',
    `icon` VARCHAR(255) DEFAULT '',
    `initial_balance` INT DEFAULT 0,
    `is_include_total` INT DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT DEFAULT 0,
    `name` VARCHAR(255) DEFAULT '',
    `type` INT DEFAULT 0,
    `icon` VARCHAR(255) DEFAULT '',
    `color` VARCHAR(20) DEFAULT '',
    `sort_order` INT DEFAULT 0,
    `is_system` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(255) DEFAULT '',
    `type` INT DEFAULT 0,
    `icon` VARCHAR(255) DEFAULT '',
    `color` VARCHAR(20) DEFAULT '',
    `parent_id` BIGINT DEFAULT 0,
    `is_system` INT DEFAULT 0,
    `source_id` BIGINT DEFAULT NULL,
    `visible` INT DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT DEFAULT NULL,
    `type` INT DEFAULT NULL,
    `amount` INT DEFAULT NULL,
    `fee` INT DEFAULT 0,
    `account_id` BIGINT DEFAULT NULL,
    `to_account_id` BIGINT DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `trans_time` DATETIME DEFAULT NULL,
    `timezone` VARCHAR(50) DEFAULT 'Asia/Shanghai',
    `remark` VARCHAR(500) DEFAULT '',
    `merchant` VARCHAR(255) DEFAULT '',
    `location` VARCHAR(255) DEFAULT '',
    `image_urls` TEXT DEFAULT NULL,
    `status` INT DEFAULT 0,
    `periodic_rule_id` BIGINT DEFAULT NULL,
    `template_id` BIGINT DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_book_trans_time` (`book_id`, `trans_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `token_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token_hash` VARCHAR(255) DEFAULT NULL,
    `expires_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_token_hash` (`token_hash`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
(11, 0, '工资', 1, '💰', '#00b894', 1, 1),
(12, 0, '兼职', 1, '💼', '#5b9bd5', 2, 1),
(13, 0, '理财', 1, '📈', '#fdcb6e', 3, 1),
(14, 0, '红包', 1, '🧧', '#ff6b6b', 4, 1),
(15, 0, '退款', 1, '↩️', '#00cec9', 5, 1),
(16, 0, '其他', 1, '💵', '#636e72', 6, 1);
