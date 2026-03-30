-- Create coupons table for internet time coupons management
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    points INT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    comment VARCHAR(500),
    username VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    timeout_seconds INT NOT NULL,
    used_count INT DEFAULT 0 NOT NULL,
    redeemed BOOLEAN DEFAULT FALSE NOT NULL,
    redeemed_by BIGINT,
    redeemed_at TIMESTAMP,
    inserted_at TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_coupons_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_coupons_redeemed_by FOREIGN KEY (redeemed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add indexes for better query performance
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_enabled ON coupons(enabled);
CREATE INDEX idx_coupons_redeemed ON coupons(redeemed);
CREATE INDEX idx_coupons_created_by ON coupons(created_by);
CREATE INDEX idx_coupons_username ON coupons(username);
