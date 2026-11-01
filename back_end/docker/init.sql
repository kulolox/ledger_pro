SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL,
    `nickname` VARCHAR(50) NOT NULL DEFAULT '',
    `avatar_url` VARCHAR(255) NOT NULL DEFAULT '',
    `phone` VARCHAR(20) NOT NULL DEFAULT '',
    `email` VARCHAR(100) NOT NULL DEFAULT '',
    `password_hash` VARCHAR(255) NOT NULL DEFAULT '',
    `reset_code` VARCHAR(10) DEFAULT NULL,
    `reset_code_expires_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `account_book` (
    `id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL DEFAULT '',
    `cover_icon` VARCHAR(255) NOT NULL DEFAULT '',
    `type` TINYINT NOT NULL DEFAULT 0,
    `owner_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL,
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
    INDEX `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL,
    `parent_id` BIGINT NOT NULL DEFAULT 0,
    `name` VARCHAR(32) NOT NULL DEFAULT '',
    `type` TINYINT NOT NULL DEFAULT 0,
    `icon` VARCHAR(50) NOT NULL DEFAULT '',
    `color` VARCHAR(20) NOT NULL DEFAULT '',
    `sort_order` INT NOT NULL DEFAULT 0,
    `is_system` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_category` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT NOT NULL DEFAULT 0,
    `name` VARCHAR(32) NOT NULL DEFAULT '',
    `type` TINYINT NOT NULL DEFAULT 0,
    `icon` VARCHAR(50) NOT NULL DEFAULT '',
    `color` VARCHAR(20) NOT NULL DEFAULT '',
    `is_system` TINYINT NOT NULL DEFAULT 0,
    `visible` TINYINT NOT NULL DEFAULT 1,
    `sort_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_type` (`user_id`, `type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL DEFAULT 0,
    `amount` INT NOT NULL DEFAULT 0,
    `account_id` BIGINT NOT NULL,
    `to_account_id` BIGINT DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `trans_time` DATETIME NOT NULL,
    `remark` VARCHAR(255) NOT NULL DEFAULT '',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_book_del_time` (`book_id`, `deleted`, `trans_time`),
    INDEX `idx_account_id` (`account_id`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `token_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token_hash` VARCHAR(64) NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
