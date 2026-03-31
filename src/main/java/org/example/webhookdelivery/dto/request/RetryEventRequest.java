package org.example.webhookdelivery.dto.request;

import jakarta.validation.constraints.Min;

/**
 * Request DTO for retrying a failed webhook event.
 */
public class RetryEventRequest {

    @Min(value = 1, message = "Max retries must be at least 1")
    private Integer maxRetries;

    public RetryEventRequest() {}

    public RetryEventRequest(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
