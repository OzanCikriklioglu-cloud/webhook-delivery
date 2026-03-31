package org.example.webhookdelivery.service.event;

import org.example.webhookdelivery.domain.WebhookEndpoint;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.domain.enums.DeliveryStatus;
import org.example.webhookdelivery.domain.enums.EventStatus;
import org.example.webhookdelivery.dto.request.CreateEventRequest;
import org.example.webhookdelivery.dto.request.RetryEventRequest;
import org.example.webhookdelivery.dto.response.DeliveryLogResponse;
import org.example.webhookdelivery.dto.response.EventResponse;
import org.example.webhookdelivery.exception.CustomException;
import org.example.webhookdelivery.repository.DeliveryLogRepository;
import org.example.webhookdelivery.repository.WebhookEndpointRepository;
import org.example.webhookdelivery.repository.WebhookEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing webhook events.
 */
@Service
@Transactional
public class EventService {

    private final WebhookEventRepository eventRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final DeliveryLogRepository deliveryLogRepository;

    public EventService(WebhookEventRepository eventRepository,
                        WebhookEndpointRepository endpointRepository,
                        DeliveryLogRepository deliveryLogRepository) {
        this.eventRepository = eventRepository;
        this.endpointRepository = endpointRepository;
        this.deliveryLogRepository = deliveryLogRepository;
    }

    /**
     * Create a new webhook event.
     */
    public EventResponse createEvent(Long userId, CreateEventRequest request) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(request.getEndpointId(), userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        if (!endpoint.isActive()) {
            throw new CustomException("Endpoint is not active", HttpStatus.BAD_REQUEST);
        }

        WebhookEvent event = new WebhookEvent(endpoint, request.getEventType(), request.getPayload());

        if (request.getMaxRetries() != null) {
            event.setMaxRetries(request.getMaxRetries());
        }

        return new EventResponse(eventRepository.save(event));
    }

    /**
     * Get all events for a user.
     * FIX: N+1 → 2 sabit sorgu (endpointIds + findByEndpointIdIn)
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getUserEvents(Long userId) {
        List<Long> endpointIds = endpointRepository.findByUserId(userId).stream()
                .map(WebhookEndpoint::getId)
                .collect(Collectors.toList());

        if (endpointIds.isEmpty()) return List.of();

        return eventRepository.findByEndpointIdIn(endpointIds).stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get events for a specific endpoint.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEndpointEvents(Long userId, Long endpointId) {
        endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        return eventRepository.findByEndpointId(endpointId).stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific event by ID.
     * FIX: findByIdWithEndpoint → JOIN FETCH, tek sorgu
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findByIdWithEndpoint(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return new EventResponse(event);
    }

    /**
     * Get a specific event by event ID (UUID).
     * FIX: findByEventId artık JOIN FETCH içeriyor, tek sorgu
     */
    @Transactional(readOnly = true)
    public EventResponse getEventByEventId(Long userId, String eventId) {
        WebhookEvent event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return new EventResponse(event);
    }

    /**
     * Get delivery logs for an event.
     * FIX: findById → findByIdWithEndpoint, lazy load ortadan kalktı
     */
    @Transactional(readOnly = true)
    public List<DeliveryLogResponse> getEventDeliveryLogs(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findByIdWithEndpoint(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return deliveryLogRepository.findByEventIdOrderByAttemptNumberDesc(eventId).stream()
                .map(DeliveryLogResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Retry a failed event.
     * FIX: findById → findByIdWithEndpoint, lazy load ortadan kalktı
     */
    public EventResponse retryEvent(Long userId, Long eventId, RetryEventRequest request) {
        WebhookEvent event = eventRepository.findByIdWithEndpoint(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        if (event.getEventStatus() != EventStatus.FAILED) {
            throw new CustomException("Only failed events can be retried", HttpStatus.BAD_REQUEST);
        }

        event.setRetryCount(0);
        event.setEventStatus(EventStatus.CREATED);
        event.setDeliveryStatus(DeliveryStatus.PENDING);

        if (request.getMaxRetries() != null) {
            event.setMaxRetries(request.getMaxRetries());
        }

        event.setNextRetryAt(null);
        event.setLastDeliveryAt(null);

        return new EventResponse(eventRepository.save(event));
    }

    /**
     * Cancel an event.
     * FIX: findById → findByIdWithEndpoint, lazy load ortadan kalktı
     */
    public EventResponse cancelEvent(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findByIdWithEndpoint(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        if (event.getEventStatus() == EventStatus.COMPLETED) {
            throw new CustomException("Cannot cancel completed events", HttpStatus.BAD_REQUEST);
        }

        event.setEventStatus(EventStatus.CANCELLED);
        event.setDeliveryStatus(DeliveryStatus.FAILED);

        return new EventResponse(eventRepository.save(event));
    }

    /**
     * Get event statistics for a user.
     * FIX: N+1 + bellekte sayma → 2 sabit sorgu, aggregate DB'de yapılıyor
     */
    @Transactional(readOnly = true)
    public EventStatistics getEventStatistics(Long userId) {
        List<Long> endpointIds = endpointRepository.findByUserId(userId).stream()
                .map(WebhookEndpoint::getId)
                .collect(Collectors.toList());

        if (endpointIds.isEmpty()) return new EventStatistics(0, 0, 0, 0);

        List<Object[]> rows = eventRepository.findStatsByEndpointIds(endpointIds);

        long total = 0, completed = 0, failed = 0, pending = 0;
        for (Object[] row : rows) {
            total     += ((Number) row[1]).longValue();
            completed += ((Number) row[2]).longValue();
            failed    += ((Number) row[3]).longValue();
            pending   += ((Number) row[4]).longValue();
        }

        return new EventStatistics(total, completed, failed, pending);
    }

    /**
     * Statistics record for event counts.
     */
    public record EventStatistics(
            long totalEvents,
            long completedEvents,
            long failedEvents,
            long pendingEvents
    ) {}
}