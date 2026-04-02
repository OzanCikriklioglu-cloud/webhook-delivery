package org.example.webhookdelivery.service.delivery;

import org.example.webhookdelivery.client.WebhookHttpClient;
import org.example.webhookdelivery.client.WebhookHttpClient.DeliveryResponse;
import org.example.webhookdelivery.domain.WebhookEndpoint;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.messaging.consumer.DeliveryConsumer.DeliveryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private WebhookHttpClient httpClient;

    @InjectMocks
    private DeliveryService deliveryService;

    private WebhookEndpoint activeEndpoint;
    private WebhookEndpoint inactiveEndpoint;

    @BeforeEach
    void setUp() {
        activeEndpoint = new WebhookEndpoint("Active", "https://example.com/webhook", 1L, "secret-key");
        activeEndpoint.setActive(true);

        inactiveEndpoint = new WebhookEndpoint("Inactive", "https://example.com/webhook", 1L, "secret-key");
        inactiveEndpoint.setActive(false);
    }

    // ── Successful delivery ────────────────────────────────────────────────────

    @Test
    void deliver_activeEndpoint_successResponse_returnsSuccess() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.success("OK", LocalDateTime.now(), 120L));

        DeliveryResult result = deliveryService.deliver(event);

        assertTrue(result.isSuccess());
        assertEquals(200, result.getHttpStatusCode());
        assertNull(result.getErrorMessage());
        assertNotNull(result.getResponseTimestamp());
    }

    // ── Inactive endpoint ──────────────────────────────────────────────────────

    @Test
    void deliver_inactiveEndpoint_returnsFailureWithoutCallingHttp() {
        WebhookEvent event = buildEvent(inactiveEndpoint);

        DeliveryResult result = deliveryService.deliver(event);

        assertFalse(result.isSuccess());
        assertEquals("Endpoint is inactive", result.getErrorMessage());
        verifyNoInteractions(httpClient);
    }

    // ── HTTP error responses ───────────────────────────────────────────────────

    @Test
    void deliver_http500Response_returnsFailure() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.failure(
                        "HTTP 500: Internal Server Error", 500, LocalDateTime.now(), 200L));

        DeliveryResult result = deliveryService.deliver(event);

        assertFalse(result.isSuccess());
        assertEquals(500, result.getHttpStatusCode());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void deliver_http503Response_returnsFailure() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.failure(
                        "HTTP 503: Service Unavailable", 503, LocalDateTime.now(), 150L));

        DeliveryResult result = deliveryService.deliver(event);

        assertFalse(result.isSuccess());
        assertEquals(503, result.getHttpStatusCode());
    }

    // ── Unreachable URL ────────────────────────────────────────────────────────

    @Test
    void deliver_unreachableUrl_returnsFailureWithNullStatusCode() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.failure(
                        "Connection refused: localhost/127.0.0.1:9999", null, LocalDateTime.now(), 30L));

        DeliveryResult result = deliveryService.deliver(event);

        assertFalse(result.isSuccess());
        assertNull(result.getHttpStatusCode());
        assertNotNull(result.getErrorMessage());
    }

    // ── Timeout ───────────────────────────────────────────────────────────────

    @Test
    void deliver_timeout_returnsFailure() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.failure(
                        "java.util.concurrent.TimeoutException: Did not observe any item within 30000ms",
                        null, LocalDateTime.now(), 30_000L));

        DeliveryResult result = deliveryService.deliver(event);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("TimeoutException"));
    }

    // ── Duration tracking ──────────────────────────────────────────────────────

    @Test
    void deliver_success_durationIsRecorded() {
        WebhookEvent event = buildEvent(activeEndpoint);

        when(httpClient.deliver(any(), any(), any(), any()))
                .thenReturn(DeliveryResponse.success("OK", LocalDateTime.now(), 350L));

        DeliveryResult result = deliveryService.deliver(event);

        assertNotNull(result.getDurationMs());
        assertEquals(350L, result.getDurationMs());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private WebhookEvent buildEvent(WebhookEndpoint endpoint) {
        WebhookEvent event = new WebhookEvent(endpoint, "payment.completed", "{\"amount\": 99}");
        // @PrePersist doesn't fire in unit tests, set eventId manually
        event.setEventId("test-event-uuid-123");
        return event;
    }
}
