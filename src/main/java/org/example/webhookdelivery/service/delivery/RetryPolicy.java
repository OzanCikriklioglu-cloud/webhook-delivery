package org.example.webhookdelivery.service.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Retry policy for webhook delivery with exponential backoff.
 * Delays: 1 min -> 5 min -> 30 min
 */
@Component
public class RetryPolicy {

    // application.yml: webhook.delivery.retry-delays: 60000,300000,1800000
    @Value("${webhook.delivery.retry-delays}")
    private List<Long> retryDelays;

    @Value("${webhook.delivery.max-retries:3}")
    private int maxRetries;

    /**
     * Calculate the delay in milliseconds for the given retry attempt.
     */
    public long getDelayMillis(int retryAttempt) {
        if (retryAttempt <= 0) {
            return retryDelays.get(0);
        }
        int index = Math.min(retryAttempt - 1, retryDelays.size() - 1);
        return retryDelays.get(index);
    }

    /**
     * Calculate the delay in minutes for the given retry attempt.
     */
    public int getDelayMinutes(int retryAttempt) {
        return (int) (getDelayMillis(retryAttempt) / 60000);
    }

    /**
     * Get the maximum number of retries allowed.
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Check if the event should be retried based on current retry count.
     */
    public boolean shouldRetry(int currentRetryCount) {
        return currentRetryCount < maxRetries;
    }
}