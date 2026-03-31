package org.example.webhookdelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new webhook event.
 */
public class CreateEventRequest {

    @NotBlank(message = "Endpoint ID is required")
    private Long endpointId;

    @NotBlank(message = "Event type is required")
    @Size(max = 255, message = "Event type must not exceed 255 characters")
    private String eventType;

    @NotBlank(message = "Payload is required")
    private String payload;

    private Integer maxRetries;

    public CreateEventRequest() {}

    public CreateEventRequest(Long endpointId, String eventType, String payload) {
        this.endpointId = endpointId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
