ALTER TABLE webhook_events ADD COLUMN idempotency_key VARCHAR(255);

-- Partial unique index: enforce uniqueness only when a key is provided
CREATE UNIQUE INDEX idx_events_idempotency_key
    ON webhook_events(endpoint_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
