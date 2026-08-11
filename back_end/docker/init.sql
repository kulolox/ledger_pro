SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nickname` VARCHAR(255) DEFAULT '',
    `avatar_url` VARCHAR(500) DEFAULT '',
    `phone` VARCHAR(20) DEFAULT '',
    `email` VARCHAR(255) DEFAULT '',
    `password_hash` VARCHAR(255) DEFAULT '',
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
    `account_id` BIGINT DEFAULT NULL,
    `to_account_id` BIGINT DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `trans_time` DATETIME DEFAULT NULL,
    `remark` VARCHAR(500) DEFAULT '',
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
