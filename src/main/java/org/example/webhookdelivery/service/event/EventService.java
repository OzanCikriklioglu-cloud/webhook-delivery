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

import java.time.LocalDateTime;
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
        // Validate endpoint exists and belongs to user
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(request.getEndpointId(), userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        // Check if endpoint is active
        if (!endpoint.isActive()) {
            throw new CustomException("Endpoint is not active", HttpStatus.BAD_REQUEST);
        }

        // Create event
        WebhookEvent event = new WebhookEvent(endpoint, request.getEventType(), request.getPayload());

        if (request.getMaxRetries() != null) {
            event.setMaxRetries(request.getMaxRetries());
        }

        WebhookEvent savedEvent = eventRepository.save(event);
        return new EventResponse(savedEvent);
    }

    /**
     * Get all events for a user.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getUserEvents(Long userId) {
        return endpointRepository.findByUserId(userId).stream()
                .flatMap(endpoint -> eventRepository.findByEndpointId(endpoint.getId()).stream())
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get events for a specific endpoint.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEndpointEvents(Long userId, Long endpointId) {
        // Validate endpoint belongs to user
        endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        return eventRepository.findByEndpointId(endpointId).stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific event by ID.
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        // Validate event belongs to user's endpoint
        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return new EventResponse(event);
    }

    /**
     * Get a specific event by event ID (UUID).
     */
    @Transactional(readOnly = true)
    public EventResponse getEventByEventId(Long userId, String eventId) {
        WebhookEvent event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        // Validate event belongs to user
        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return new EventResponse(event);
    }

    /**
     * Get delivery logs for an event.
     */
    @Transactional(readOnly = true)
    public List<DeliveryLogResponse> getEventDeliveryLogs(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        // Validate event belongs to user
        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        return deliveryLogRepository.findByEventIdOrderByAttemptNumberDesc(eventId).stream()
                .map(DeliveryLogResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Retry a failed event.
     */
    public EventResponse retryEvent(Long userId, Long eventId, RetryEventRequest request) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        // Validate event belongs to user
        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        // Only allow retry for failed events
        if (event.getEventStatus() != EventStatus.FAILED) {
            throw new CustomException("Only failed events can be retried", HttpStatus.BAD_REQUEST);
        }

        // Reset retry count and update status
        event.setRetryCount(0);
        event.setEventStatus(EventStatus.CREATED);
        event.setDeliveryStatus(DeliveryStatus.PENDING);

        if (request.getMaxRetries() != null) {
            event.setMaxRetries(request.getMaxRetries());
        }

        event.setNextRetryAt(null);
        event.setLastDeliveryAt(null);

        WebhookEvent updatedEvent = eventRepository.save(event);
        return new EventResponse(updatedEvent);
    }

    /**
     * Cancel an event.
     */
    public EventResponse cancelEvent(Long userId, Long eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("Event not found", HttpStatus.NOT_FOUND));

        // Validate event belongs to user
        if (!event.getEndpoint().getUserId().equals(userId)) {
            throw new CustomException("Event not found", HttpStatus.NOT_FOUND);
        }

        // Only allow cancellation for events that are not completed
        if (event.getEventStatus() == EventStatus.COMPLETED) {
            throw new CustomException("Cannot cancel completed events", HttpStatus.BAD_REQUEST);
        }

        event.setEventStatus(EventStatus.CANCELLED);
        event.setDeliveryStatus(DeliveryStatus.FAILED);

        WebhookEvent updatedEvent = eventRepository.save(event);
        return new EventResponse(updatedEvent);
    }

    /**
     * Get event statistics for a user.
     */
    @Transactional(readOnly = true)
    public EventStatistics getEventStatistics(Long userId) {
        List<WebhookEndpoint> endpoints = endpointRepository.findByUserId(userId);

        long totalEvents = 0;
        long completedEvents = 0;
        long failedEvents = 0;
        long pendingEvents = 0;

        for (WebhookEndpoint endpoint : endpoints) {
            List<WebhookEvent> events = eventRepository.findByEndpointId(endpoint.getId());
            totalEvents += events.size();
            completedEvents += events.stream()
                    .filter(e -> e.getEventStatus() == EventStatus.COMPLETED)
                    .count();
            failedEvents += events.stream()
                    .filter(e -> e.getEventStatus() == EventStatus.FAILED)
                    .count();
            pendingEvents += events.stream()
                    .filter(e -> e.getEventStatus() == EventStatus.CREATED ||
                            e.getEventStatus() == EventStatus.PROCESSING)
                    .count();
        }

        return new EventStatistics(totalEvents, completedEvents, failedEvents, pendingEvents);
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
