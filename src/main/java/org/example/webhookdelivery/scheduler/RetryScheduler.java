package org.example.webhookdelivery.scheduler;

import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.domain.enums.DeliveryStatus;
import org.example.webhookdelivery.messaging.publisher.EventPublisher;
import org.example.webhookdelivery.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically scans for stuck events and re-queues them.
 *
 * Two cases handled:
 *  1. RETRYING events whose nextRetryAt has passed but were never re-queued
 *     (e.g. RabbitMQ was down and the message was lost).
 *  2. Events stuck in DELIVERING state for more than 5 minutes
 *     (e.g. worker crashed mid-delivery).
 */
@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final WebhookEventRepository eventRepository;
    private final EventPublisher eventPublisher;

    public RetryScheduler(WebhookEventRepository eventRepository, EventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${webhook.scheduler.interval-ms:60000}")
    @Transactional
    public void checkStuckEvents() {
        requeueRetryingEvents();
        resetStuckDeliveringEvents();
    }

    /**
     * Find RETRYING events whose nextRetryAt has passed and re-queue them.
     * These are events that should have been retried by now but weren't
     * (message lost from RabbitMQ, app restart, etc.).
     */
    private void requeueRetryingEvents() {
        List<WebhookEvent> events = eventRepository.findEventsReadyForRetry(LocalDateTime.now());

        if (events.isEmpty()) return;

        log.info("RetryScheduler: found {} event(s) past their retry time, re-queuing", events.size());

        for (WebhookEvent event : events) {
            log.info("Re-queuing stuck event {} (retryCount: {})", event.getEventId(), event.getRetryCount());
            eventPublisher.publishForDelivery(event);
        }
    }

    /**
     * Find events stuck in DELIVERING state for more than 5 minutes and reset them.
     * These are events where the worker crashed mid-delivery and never updated the status.
     */
    private void resetStuckDeliveringEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<WebhookEvent> stuckEvents = eventRepository.findStuckEvents(threshold, DeliveryStatus.DELIVERING);

        if (stuckEvents.isEmpty()) return;

        log.warn("RetryScheduler: found {} event(s) stuck in DELIVERING state, resetting", stuckEvents.size());

        for (WebhookEvent event : stuckEvents) {
            log.warn("Resetting stuck event {} back to RETRYING", event.getEventId());
            event.setDeliveryStatus(DeliveryStatus.RETRYING);
            event.setNextRetryAt(LocalDateTime.now());
            eventRepository.save(event);
            eventPublisher.publishForDelivery(event);
        }
    }
}
