ALTER TABLE `user`
    ADD COLUMN `reset_code` VARCHAR(10) DEFAULT NULL COMMENT '密码重置验证码',
    ADD COLUMN `reset_code_expires_at` DATETIME DEFAULT NULL COMMENT '验证码过期时间';

CREATE TABLE IF NOT EXISTS `token_blacklist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token_hash` VARCHAR(64) NOT NULL UNIQUE COMMENT 'SHA-256(token)',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
