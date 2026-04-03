package org.example.webhookdelivery.domain.enums;
/**
 * Represents the delivery status of a webhook event.
 */
public enum DeliveryStatus {
    PENDING,       // Event created, waiting to be processed
    QUEUED,        // Event pushed to RabbitMQ queue
    DELIVERING,    // Currently attempting delivery
    DELIVERED,     // Successfully delivered (HTTP 2xx)
    FAILED,        // Delivery failed after all retries
    RETRYING,      // Delivery failed, scheduled for retry
    CANCELLED      // Event cancelled by user
}
