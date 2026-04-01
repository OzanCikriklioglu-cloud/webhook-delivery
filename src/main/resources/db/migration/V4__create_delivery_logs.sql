-- V4: Create delivery_logs table
CREATE TABLE IF NOT EXISTS delivery_logs (
                                             id BIGSERIAL PRIMARY KEY,
                                             event_id BIGINT NOT NULL,
                                             attempt_number INT NOT NULL DEFAULT 1,
                                             status VARCHAR(50) NOT NULL,
    http_status_code INT,
    response_body TEXT,
    error_message TEXT,
    request_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_timestamp TIMESTAMP,
    duration_ms BIGINT,

    CONSTRAINT fk_logs_event FOREIGN KEY (event_id) REFERENCES webhook_events(id) ON DELETE CASCADE
    );

CREATE INDEX idx_logs_event_id ON delivery_logs(event_id);
CREATE INDEX idx_logs_attempt ON delivery_logs(event_id, attempt_number DESC);
CREATE INDEX idx_logs_status ON delivery_logs(status);
