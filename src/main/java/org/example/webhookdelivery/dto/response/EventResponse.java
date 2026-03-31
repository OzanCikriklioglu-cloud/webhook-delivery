package org.example.webhookdelivery.dto.response;

import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.domain.enums.DeliveryStatus;
import org.example.webhookdelivery.domain.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for webhook event data.
 */
public class EventResponse {

    private Long id;
    private String eventId;
    private Long endpointId;
    private String endpointName;
    private String eventType;
    private String payload;
    private EventStatus eventStatus;
    private DeliveryStatus deliveryStatus;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime nextRetryAt;
    private LocalDateTime lastDeliveryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventResponse() {}

    public EventResponse(WebhookEvent event) {
        this.id = event.getId();
        this.eventId = event.getEventId();
        this.endpointId = event.getEndpoint().getId();
        this.endpointName = event.getEndpoint().getName();
        this.eventType = event.getEventType();
        this.payload = event.getPayload();
        this.eventStatus = event.getEventStatus();
        this.deliveryStatus = event.getDeliveryStatus();
        this.retryCount = event.getRetryCount();
        this.maxRetries = event.getMaxRetries();
        this.nextRetryAt = event.getNextRetryAt();
        this.lastDeliveryAt = event.getLastDeliveryAt();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final EventResponse response = new EventResponse();

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder eventId(String eventId) {
            response.eventId = eventId;
            return this;
        }

        public Builder endpointId(Long endpointId) {
            response.endpointId = endpointId;
            return this;
        }

        public Builder endpointName(String endpointName) {
            response.endpointName = endpointName;
            return this;
        }

        public Builder eventType(String eventType) {
            response.eventType = eventType;
            return this;
        }

        public Builder payload(String payload) {
            response.payload = payload;
            return this;
        }

        public Builder eventStatus(EventStatus eventStatus) {
            response.eventStatus = eventStatus;
            return this;
        }

        public Builder deliveryStatus(DeliveryStatus deliveryStatus) {
            response.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder retryCount(int retryCount) {
            response.retryCount = retryCount;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            response.maxRetries = maxRetries;
            return this;
        }

        public Builder nextRetryAt(LocalDateTime nextRetryAt) {
            response.nextRetryAt = nextRetryAt;
            return this;
        }

        public Builder lastDeliveryAt(LocalDateTime lastDeliveryAt) {
            response.lastDeliveryAt = lastDeliveryAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public EventResponse build() {
            return response;
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
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

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public LocalDateTime getLastDeliveryAt() {
        return lastDeliveryAt;
    }

    public void setLastDeliveryAt(LocalDateTime lastDeliveryAt) {
        this.lastDeliveryAt = lastDeliveryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
