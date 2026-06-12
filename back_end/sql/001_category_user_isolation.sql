-- 用户分类偏好表：按用户隔离 visible/sort_order
CREATE TABLE IF NOT EXISTS user_category_preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    visible INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_category (user_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- category 表加 owner_id
ALTER TABLE category ADD COLUMN owner_id BIGINT DEFAULT NULL AFTER is_system;
