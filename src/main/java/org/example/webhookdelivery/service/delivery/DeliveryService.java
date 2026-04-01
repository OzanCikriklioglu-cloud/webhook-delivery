package org.example.webhookdelivery.service.delivery;

import org.example.webhookdelivery.client.WebhookHttpClient;
import org.example.webhookdelivery.domain.WebhookEndpoint;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.messaging.consumer.DeliveryConsumer.DeliveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final WebhookHttpClient httpClient;

    public DeliveryService(WebhookHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Deliver a webhook event to its target endpoint.
     */
    public DeliveryResult deliver(WebhookEvent event) {
        WebhookEndpoint endpoint = event.getEndpoint();

        if (!endpoint.isActive()) {
            log.warn("Endpoint {} is inactive, skipping delivery", endpoint.getId());
            LocalDateTime now = LocalDateTime.now();
            return DeliveryResult.failure(
                    "Endpoint is inactive",
                    null,
                    now,
                    now,
                    0L
            );
        }

        String targetUrl = endpoint.getUrl();
        String payload = event.getPayload();
        String secretKey = endpoint.getSecretKey();
        String eventId = event.getEventId();

        log.info("Delivering event {} to {}", eventId, targetUrl);

        WebhookHttpClient.DeliveryResponse response = httpClient.deliver(
                targetUrl, payload, secretKey, eventId
        );

        if (response.success()) {
            LocalDateTime requestTime = response.responseTimestamp().minusNanos(response.durationMs() * 1_000_000);
            return DeliveryResult.success(
                    response.httpStatusCode(),
                    response.responseBody(),
                    requestTime,
                    response.responseTimestamp(),
                    response.durationMs()
            );
        } else {
            LocalDateTime requestTime = response.responseTimestamp().minusNanos(response.durationMs() * 1_000_000);
            return DeliveryResult.failure(
                    response.errorMessage(),
                    response.httpStatusCode(),
                    requestTime,
                    response.responseTimestamp(),
                    response.durationMs()
            );
        }
    }
}
