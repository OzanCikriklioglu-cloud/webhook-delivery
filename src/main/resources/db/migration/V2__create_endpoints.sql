-- V2: Create webhook_endpoints table
CREATE TABLE IF NOT EXISTS webhook_endpoints (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 name VARCHAR(255) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    user_id BIGINT NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_endpoints_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE INDEX idx_endpoints_user_id ON webhook_endpoints(user_id);
CREATE INDEX idx_endpoints_user_active ON webhook_endpoints(user_id, is_active);
CREATE UNIQUE INDEX idx_endpoints_url_user ON webhook_endpoints(url, user_id);
