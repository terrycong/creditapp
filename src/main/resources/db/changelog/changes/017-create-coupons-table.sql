--liquibase formatted sql

--changeset admin:017-create-coupons-table
--comment: Create coupons table for internet access voucher management
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Coupon code',
    points INT NOT NULL COMMENT 'Points value',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Is enabled',
    comment VARCHAR(500) COMMENT 'Comment/note',
    username VARCHAR(50) COMMENT 'Assigned username (optional)',
    timeout_seconds INT NOT NULL COMMENT 'Timeout in seconds',
    used_count INT NOT NULL DEFAULT 0 COMMENT 'Usage count',
    redeemed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Is redeemed',
    redeemed_by BIGINT COMMENT 'Redeemed by user ID',
    redeemed_at DATETIME COMMENT 'Redeemed timestamp',
    created_by BIGINT COMMENT 'Created by user ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created timestamp',
    inserted_at DATETIME COMMENT 'Inserted timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated timestamp',
    CONSTRAINT fk_coupons_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_coupons_redeemed_by FOREIGN KEY (redeemed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Internet access coupons';

--changeset admin:017-add-coupon-indexes
--comment: Add indexes for coupons table
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_enabled ON coupons(enabled);
CREATE INDEX idx_coupons_username ON coupons(username);
CREATE INDEX idx_coupons_redeemed ON coupons(redeemed);
CREATE INDEX idx_coupons_created_by ON coupons(created_by);
