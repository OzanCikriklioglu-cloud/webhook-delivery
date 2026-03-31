-- V4: Create delivery_logs table for tracking all delivery attempts
CREATE TABLE delivery_logs (
                               id BIGSERIAL PRIMARY KEY,
                               event_id BIGINT NOT NULL,
                               attempt_number INT NOT NULL DEFAULT 1,
                               status VARCHAR(50) NOT NULL,
                               http_status_code INT,
                               response_body TEXT,
                               error_message TEXT,
                               request_timestamp TIMESTAMP NOT NULL,
                               response_timestamp TIMESTAMP,
                               duration_ms BIGINT,

                               CONSTRAINT fk_log_event FOREIGN KEY (event_id) REFERENCES webhook_events(id) ON DELETE CASCADE,
                               CONSTRAINT chk_log_status CHECK (status IN ('SUCCESS', 'FAILED', 'TIMEOUT', 'ERROR'))
);

-- Index for logs by event
CREATE INDEX idx_logs_event_id ON delivery_logs(event_id);

-- Index for recent failures
CREATE INDEX idx_logs_failed ON delivery_logs(status, request_timestamp DESC) WHERE status != 'SUCCESS';
