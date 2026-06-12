-- 用户分类表（替换 user_category_preference）
CREATE TABLE IF NOT EXISTS user_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL DEFAULT 0,
    icon VARCHAR(50) DEFAULT '',
    color VARCHAR(20) DEFAULT '',
    parent_id BIGINT DEFAULT 0,
    is_system INT DEFAULT 0,
    source_id BIGINT DEFAULT NULL,
    visible INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- category 表移除用户相关字段
ALTER TABLE category DROP COLUMN IF EXISTS visible;
ALTER TABLE category DROP COLUMN IF EXISTS sort_order;
ALTER TABLE category DROP COLUMN IF EXISTS owner_id;
ALTER TABLE category DROP COLUMN IF EXISTS deleted;
ALTER TABLE category DROP COLUMN IF EXISTS updated_at;

-- 清理旧表
DROP TABLE IF EXISTS user_category_preference;
