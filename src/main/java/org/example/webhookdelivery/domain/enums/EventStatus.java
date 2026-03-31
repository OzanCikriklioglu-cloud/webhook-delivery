package org.example.webhookdelivery.domain.enums;
/**
 * Represents the status of a webhook event.
 */
public enum EventStatus {
    CREATED,       // Event created by user
    PROCESSING,    // Event is being processed
    COMPLETED,    // Event successfully delivered
    FAILED,       // Event delivery failed after all retries
    CANCELLED     // Event cancelled by user
}
