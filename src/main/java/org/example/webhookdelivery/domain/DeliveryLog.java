package org.example.webhookdelivery.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a delivery attempt log entry for webhook events.
 * Tracks every delivery attempt with response details.
 */
@Entity
@Table(name = "delivery_logs")
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private WebhookEvent event;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    @Column(nullable = false)
    private String status;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp")
    private LocalDateTime responseTimestamp;

    @Column(name = "duration_ms")
    private Long durationMs;

    @PrePersist
    protected void onCreate() {
        if (this.requestTimestamp == null) {
            this.requestTimestamp = LocalDateTime.now();
        }
    }

    // Constructors
    public DeliveryLog() {}

    public DeliveryLog(WebhookEvent event, int attemptNumber, String status) {
        this.event = event;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.requestTimestamp = LocalDateTime.now();
    }

    // Builder pattern for easier construction
    public static DeliveryLogBuilder builder() {
        return new DeliveryLogBuilder();
    }

    public static class DeliveryLogBuilder {
        private final DeliveryLog log;

        public DeliveryLogBuilder() {
            this.log = new DeliveryLog();
        }

        public DeliveryLogBuilder event(WebhookEvent event) {
            log.event = event;
            return this;
        }

        public DeliveryLogBuilder attemptNumber(int attemptNumber) {
            log.attemptNumber = attemptNumber;
            return this;
        }

        public DeliveryLogBuilder status(String status) {
            log.status = status;
            return this;
        }

        public DeliveryLogBuilder httpStatusCode(Integer httpStatusCode) {
            log.httpStatusCode = httpStatusCode;
            return this;
        }

        public DeliveryLogBuilder responseBody(String responseBody) {
            log.responseBody = responseBody;
            return this;
        }

        public DeliveryLogBuilder errorMessage(String errorMessage) {
            log.errorMessage = errorMessage;
            return this;
        }

        public DeliveryLogBuilder requestTimestamp(LocalDateTime requestTimestamp) {
            log.requestTimestamp = requestTimestamp;
            return this;
        }

        public DeliveryLogBuilder responseTimestamp(LocalDateTime responseTimestamp) {
            log.responseTimestamp = responseTimestamp;
            return this;
        }

        public DeliveryLogBuilder durationMs(Long durationMs) {
            log.durationMs = durationMs;
            return this;
        }

        public DeliveryLog build() {
            return log;
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WebhookEvent getEvent() {
        return event;
    }

    public void setEvent(WebhookEvent event) {
        this.event = event;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
