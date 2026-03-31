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

    // ── Mevcut metodlar (dokunulmadı) ────────────────────────────────────────

    List<WebhookEvent> findByEndpointId(Long endpointId);

    List<WebhookEvent> findByEndpointIdAndEventStatus(Long endpointId, EventStatus eventStatus);

    List<WebhookEvent> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    List<WebhookEvent> findByEventStatus(EventStatus eventStatus);

    long countByEndpointIdAndEventStatus(Long endpointId, EventStatus eventStatus);

    @Query("SELECT e FROM WebhookEvent e WHERE e.nextRetryAt <= :now AND e.deliveryStatus = 'RETRYING' AND e.retryCount < e.maxRetries")
    List<WebhookEvent> findEventsReadyForRetry(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM WebhookEvent e WHERE e.deliveryStatus = :status AND e.lastDeliveryAt < :threshold")
    List<WebhookEvent> findStuckEvents(@Param("threshold") LocalDateTime threshold, @Param("status") DeliveryStatus status);

    // ── N+1 Fix 1: eventId ile tek sorguda endpoint'i de çek ─────────────────
    // Kullanım: getEventByEventId(), (eski: findByEventId + lazy load)
    @Query("SELECT e FROM WebhookEvent e JOIN FETCH e.endpoint WHERE e.eventId = :eventId")
    Optional<WebhookEvent> findByEventId(String eventId);

    // ── N+1 Fix 2: id ile tek sorguda endpoint'i de çek ──────────────────────
    // Kullanım: getEvent(), retryEvent(), cancelEvent(), getEventDeliveryLogs()
    @Query("SELECT e FROM WebhookEvent e JOIN FETCH e.endpoint WHERE e.id = :id")
    Optional<WebhookEvent> findByIdWithEndpoint(@Param("id") Long id);

    // ── N+1 Fix 3: birden fazla endpoint için tek sorguda tüm event'ler ───────
    // Kullanım: getUserEvents()
    @Query("SELECT e FROM WebhookEvent e WHERE e.endpoint.id IN :endpointIds")
    List<WebhookEvent> findByEndpointIdIn(@Param("endpointIds") List<Long> endpointIds);

    // ── N+1 Fix 4: istatistikleri DB'de aggregate et, bellekte sayma ──────────
    // Kullanım: getEventStatistics()
    // Dönüş sırası: [endpointId, total, completed, failed, pending]
    @Query("""
            SELECT e.endpoint.id,
                   COUNT(e),
                   SUM(CASE WHEN e.eventStatus = org.example.webhookdelivery.domain.enums.EventStatus.COMPLETED  THEN 1 ELSE 0 END),
                   SUM(CASE WHEN e.eventStatus = org.example.webhookdelivery.domain.enums.EventStatus.FAILED     THEN 1 ELSE 0 END),
                   SUM(CASE WHEN e.eventStatus IN (
                       org.example.webhookdelivery.domain.enums.EventStatus.CREATED,
                       org.example.webhookdelivery.domain.enums.EventStatus.PROCESSING
                   ) THEN 1 ELSE 0 END)
            FROM WebhookEvent e
            WHERE e.endpoint.id IN :endpointIds
            GROUP BY e.endpoint.id
            """)
    List<Object[]> findStatsByEndpointIds(@Param("endpointIds") List<Long> endpointIds);
}