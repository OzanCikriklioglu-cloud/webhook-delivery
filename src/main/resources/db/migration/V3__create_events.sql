-- V3: Create webhook_events table
CREATE TABLE webhook_events (
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

                                CONSTRAINT fk_event_endpoint FOREIGN KEY (endpoint_id) REFERENCES webhook_endpoints(id) ON DELETE CASCADE,
                                CONSTRAINT chk_event_status CHECK (event_status IN ('CREATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
                                CONSTRAINT chk_delivery_status CHECK (delivery_status IN ('PENDING', 'QUEUED', 'DELIVERING', 'DELIVERED', 'FAILED', 'RETRYING'))
);

-- Index for event lookup by event_id
CREATE INDEX idx_events_event_id ON webhook_events(event_id);

-- Index for events by endpoint
CREATE INDEX idx_events_endpoint_id ON webhook_events(endpoint_id);

-- Index for pending events (retry scheduling)
CREATE INDEX idx_events_next_retry ON webhook_events(next_retry_at) WHERE next_retry_at IS NOT NULL;

-- Index for events by status
CREATE INDEX idx_events_status ON webhook_events(event_status, delivery_status);
