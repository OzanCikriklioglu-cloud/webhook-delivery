package org.example.webhookdelivery.dto.response;
import org.example.webhookdelivery.domain.WebhookEndpoint;

import java.time.LocalDateTime;

/**
 * Response DTO for webhook endpoint data.
 */
public class EndpointResponse {

    private Long id;
    private String name;
    private String url;
    private String secretKey;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EndpointResponse() {}

    public EndpointResponse(WebhookEndpoint endpoint) {
        this.id = endpoint.getId();
        this.name = endpoint.getName();
        this.url = endpoint.getUrl();
        this.secretKey = endpoint.getSecretKey();
        this.isActive = endpoint.isActive();
        this.createdAt = endpoint.getCreatedAt();
        this.updatedAt = endpoint.getUpdatedAt();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final EndpointResponse response = new EndpointResponse();

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder name(String name) {
            response.name = name;
            return this;
        }

        public Builder url(String url) {
            response.url = url;
            return this;
        }

        public Builder secretKey(String secretKey) {
            response.secretKey = secretKey;
            return this;
        }

        public Builder isActive(boolean isActive) {
            response.isActive = isActive;
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

        public EndpointResponse build() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
