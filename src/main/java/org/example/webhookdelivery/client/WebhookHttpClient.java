package org.example.webhookdelivery.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Component
public class WebhookHttpClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookHttpClient.class);

    private final WebClient webClient;

    @Value("${webhook.delivery.timeout-seconds:30}")
    private int timeoutSeconds;

    public WebhookHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Deliver a webhook payload to the target URL.
     */
    public DeliveryResponse deliver(String targetUrl, String payload, String secretKey, String eventId) {
        LocalDateTime requestTime = LocalDateTime.now();

        try {
            String signature = computeHmacSignature(payload, secretKey);

            String response = webClient.post()
                    .uri(targetUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("X-Webhook-Event", eventId)
                    .header("X-Webhook-Signature", signature)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            LocalDateTime responseTime = LocalDateTime.now();
            long durationMs = Duration.between(requestTime, responseTime).toMillis();

            log.info("Webhook delivered to {} in {}ms", targetUrl, durationMs);

            return DeliveryResponse.success(response, responseTime, durationMs);

        } catch (WebClientResponseException e) {
            LocalDateTime responseTime = LocalDateTime.now();
            long durationMs = Duration.between(requestTime, responseTime).toMillis();

            log.warn("Webhook delivery to {} returned HTTP {}: {}",
                    targetUrl, e.getStatusCode(), e.getMessage());

            return DeliveryResponse.failure(
                    "HTTP " + e.getStatusCode().value() + ": " + e.getMessage(),
                    e.getStatusCode().value(),
                    responseTime,
                    durationMs
            );

        } catch (Exception e) {
            LocalDateTime responseTime = LocalDateTime.now();
            long durationMs = Duration.between(requestTime, responseTime).toMillis();

            log.error("Webhook delivery to {} failed: {}", targetUrl, e.getMessage());

            return DeliveryResponse.failure(
                    e.getMessage(),
                    null,
                    responseTime,
                    durationMs
            );
        }
    }

    /**
     * Compute HMAC-SHA256 signature of the payload using the endpoint's secret key.
     * Format: "sha256=<hex>" — same convention used by Stripe and GitHub webhooks.
     *
     * Receivers can verify by computing the same HMAC on their side and comparing.
     */
    private String computeHmacSignature(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    /**
     * Check if an HTTP status code indicates success (2xx).
     */
    public static boolean isSuccessStatus(Integer statusCode) {
        return statusCode != null && statusCode >= 200 && statusCode < 300;
    }

    /**
     * Delivery response result.
     */
    public record DeliveryResponse(
            boolean success,
            String responseBody,
            Integer httpStatusCode,
            String errorMessage,
            LocalDateTime responseTimestamp,
            Long durationMs
    ) {
        public static DeliveryResponse success(String body, LocalDateTime time, Long duration) {
            return new DeliveryResponse(true, body, 200, null, time, duration);
        }

        public static DeliveryResponse failure(String error, Integer status,
                                               LocalDateTime time, Long duration) {
            return new DeliveryResponse(false, null, status, error, time, duration);
        }
    }
}
