package org.example.webhookdelivery.controller;

import org.example.webhookdelivery.domain.User;
import org.example.webhookdelivery.dto.request.CreateEventRequest;
import org.example.webhookdelivery.dto.request.RetryEventRequest;
import org.example.webhookdelivery.dto.response.DeliveryLogResponse;
import org.example.webhookdelivery.dto.response.EventResponse;
import org.example.webhookdelivery.repository.UserRepository;
import org.example.webhookdelivery.service.event.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing webhook events.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    public EventController(EventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new webhook event.
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEventRequest request) {

        Long userId = getUserId(userDetails);
        EventResponse response = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all events for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getUserEvents(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        List<EventResponse> events = eventService.getUserEvents(userId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get events for a specific endpoint.
     */
    @GetMapping("/endpoint/{endpointId}")
    public ResponseEntity<List<EventResponse>> getEndpointEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long endpointId) {

        Long userId = getUserId(userDetails);
        List<EventResponse> events = eventService.getEndpointEvents(userId, endpointId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get a specific event by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        EventResponse response = eventService.getEvent(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific event by event UUID.
     */
    @GetMapping("/by-event-id/{eventId}")
    public ResponseEntity<EventResponse> getEventByEventId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String eventId) {

        Long userId = getUserId(userDetails);
        EventResponse response = eventService.getEventByEventId(userId, eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get delivery logs for an event.
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<DeliveryLogResponse>> getEventDeliveryLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        List<DeliveryLogResponse> logs = eventService.getEventDeliveryLogs(userId, id);
        return ResponseEntity.ok(logs);
    }

    /**
     * Retry a failed event.
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<EventResponse> retryEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) RetryEventRequest request) {

        Long userId = getUserId(userDetails);
        if (request == null) {
            request = new RetryEventRequest();
        }
        EventResponse response = eventService.retryEvent(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an event.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        EventResponse response = eventService.cancelEvent(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get event statistics for the authenticated user.
     */
    @GetMapping("/statistics")
    public ResponseEntity<EventService.EventStatistics> getEventStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        EventService.EventStatistics statistics = eventService.getEventStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get user ID from UserDetails.
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
