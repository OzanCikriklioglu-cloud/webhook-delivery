package org.example.webhookdelivery.dto.response;

import org.example.webhookdelivery.domain.DeliveryLog;

import java.time.LocalDateTime;

/**
 * Response DTO for delivery log data.
 */
public class DeliveryLogResponse {

    private Long id;
    private Long eventId;
    private String eventIdString;
    private int attemptNumber;
    private String status;
    private Integer httpStatusCode;
    private String responseBody;
    private String errorMessage;
    private LocalDateTime requestTimestamp;
    private LocalDateTime responseTimestamp;
    private Long durationMs;

    public DeliveryLogResponse() {}

    public DeliveryLogResponse(DeliveryLog log) {
        this.id = log.getId();
        this.eventId = log.getEvent().getId();
        this.eventIdString = log.getEvent().getEventId();
        this.attemptNumber = log.getAttemptNumber();
        this.status = log.getStatus();
        this.httpStatusCode = log.getHttpStatusCode();
        this.responseBody = log.getResponseBody();
        this.errorMessage = log.getErrorMessage();
        this.requestTimestamp = log.getRequestTimestamp();
        this.responseTimestamp = log.getResponseTimestamp();
        this.durationMs = log.getDurationMs();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventIdString() {
        return eventIdString;
    }

    public void setEventIdString(String eventIdString) {
        this.eventIdString = eventIdString;
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
