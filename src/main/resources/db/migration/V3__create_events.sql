-- V3: Create webhook_events table
CREATE TABLE IF NOT EXISTS webhook_events (
                                              id BIGSERIAL PRIMARY KEY,
                                              event_id VARCHAR(36) NOT NULL UNIQUE,
    endpoint_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    event_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    delivery_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,
    last_delivery_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_endpoint FOREIGN KEY (endpoint_id) REFERENCES webhook_endpoints(id) ON DELETE CASCADE
    );

CREATE INDEX idx_events_event_id ON webhook_events(event_id);
CREATE INDEX idx_events_endpoint_id ON webhook_events(endpoint_id);
CREATE INDEX idx_events_status ON webhook_events(event_status);
CREATE INDEX idx_events_delivery_status ON webhook_events(delivery_status);
CREATE INDEX idx_events_next_retry ON webhook_events(next_retry_at) WHERE delivery_status = 'RETRYING';
CREATE INDEX idx_events_stuck ON webhook_events(last_delivery_at) WHERE delivery_status = 'DELIVERING';
