-- V2: Create webhook_endpoints table
CREATE TABLE webhook_endpoints (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL,
                                   url VARCHAR(2048) NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   secret_key VARCHAR(512) NOT NULL,
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_endpoint_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                   CONSTRAINT chk_endpoint_url CHECK (url ~ '^https?://')
    );

-- Index for faster queries by user
CREATE INDEX idx_endpoints_user_id ON webhook_endpoints(user_id);

-- Index for active endpoints lookup
CREATE INDEX idx_endpoints_active ON webhook_endpoints(is_active) WHERE is_active = TRUE;
