package org.example.webhookdelivery.repository;

import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.domain.enums.DeliveryStatus;
import org.example.webhookdelivery.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    List<WebhookEvent> findByEndpointId(Long endpointId);

    List<WebhookEvent> findByEndpointIdAndEventStatus(Long endpointId, EventStatus eventStatus);

    List<WebhookEvent> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    List<WebhookEvent> findByEventStatus(EventStatus eventStatus);

    // Find events ready for retry (next_retry_at <= now and retry_count < max_retries)
    @Query("SELECT e FROM WebhookEvent e WHERE e.nextRetryAt <= :now AND e.deliveryStatus = 'RETRYING' AND e.retryCount < e.maxRetries")
    List<WebhookEvent> findEventsReadyForRetry(@Param("now") LocalDateTime now);

    // Find events stuck in processing (no update for a long time)
    @Query("SELECT e FROM WebhookEvent e WHERE e.deliveryStatus = 'DELIVERING' AND e.lastDeliveryAt < :threshold")
    List<WebhookEvent> findStuckEvents(@Param("threshold") LocalDateTime threshold);

    // Count events by status for an endpoint
    long countByEndpointIdAndEventStatus(Long endpointId, EventStatus eventStatus);
}
