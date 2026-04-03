package org.example.webhookdelivery.messaging.consumer;

import org.example.webhookdelivery.config.RabbitMQConfig;
import org.example.webhookdelivery.domain.DeliveryLog;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.example.webhookdelivery.domain.enums.DeliveryStatus;
import org.example.webhookdelivery.domain.enums.EventStatus;
import org.example.webhookdelivery.messaging.publisher.EventPublisher;
import org.example.webhookdelivery.repository.DeliveryLogRepository;
import org.example.webhookdelivery.repository.WebhookEventRepository;
import org.example.webhookdelivery.service.delivery.DeliveryService;
import org.example.webhookdelivery.service.delivery.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryConsumer.class);

    private final DeliveryService deliveryService;
    private final WebhookEventRepository eventRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final EventPublisher eventPublisher;
    private final RetryPolicy retryPolicy;

    private final Counter deliverySuccessCounter;
    private final Counter deliveryFailureCounter;
    private final Counter deliveryRetryCounter;

    public DeliveryConsumer(DeliveryService deliveryService,
                            WebhookEventRepository eventRepository,
                            DeliveryLogRepository deliveryLogRepository,
                            EventPublisher eventPublisher,
                            RetryPolicy retryPolicy,
                            MeterRegistry meterRegistry) {
        this.deliveryService = deliveryService;
        this.eventRepository = eventRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.eventPublisher = eventPublisher;
        this.retryPolicy = retryPolicy;

        this.deliverySuccessCounter = Counter.builder("webhook.delivery.success")
                .description("Number of webhooks successfully delivered")
                .register(meterRegistry);
        this.deliveryFailureCounter = Counter.builder("webhook.delivery.failure")
                .description("Number of webhooks that exhausted all retries and failed")
                .register(meterRegistry);
        this.deliveryRetryCounter = Counter.builder("webhook.delivery.retry")
                .description("Number of webhook delivery retries scheduled")
                .register(meterRegistry);
    }

    /**
     * Listen for events from the main delivery queue.
     */
    @RabbitListener(queues = RabbitMQConfig.DELIVERY_QUEUE)
    @Transactional
    public void handleDelivery(WebhookEvent event) {
        log.info("Received event {} from delivery queue", event.getEventId());

        try {
            WebhookEvent freshEvent = eventRepository.findByIdWithEndpoint(event.getId())
                    .orElseThrow(() -> new RuntimeException("Event not found: " + event.getId()));

            if (freshEvent.getEventStatus() == EventStatus.COMPLETED ||
                    freshEvent.getEventStatus() == EventStatus.CANCELLED) {
                log.info("Event {} already in terminal state, skipping", freshEvent.getEventId());
                return;
            }

            freshEvent.setEventStatus(EventStatus.PROCESSING);
            freshEvent.setDeliveryStatus(DeliveryStatus.DELIVERING);
            freshEvent.setLastDeliveryAt(LocalDateTime.now());
            eventRepository.save(freshEvent);

            DeliveryResult result = deliveryService.deliver(freshEvent);

            if (result.isSuccess()) {
                handleSuccess(freshEvent, result);
            } else {
                handleFailure(freshEvent, result);
            }

        } catch (Exception e) {
            log.error("Error processing event {}: {}", event.getEventId(), e.getMessage(), e);
            handleProcessingError(event, e);
        }
    }

    private void handleSuccess(WebhookEvent event, DeliveryResult result) {
        log.info("Event {} delivered successfully", event.getEventId());

        event.setEventStatus(EventStatus.COMPLETED);
        event.setDeliveryStatus(DeliveryStatus.DELIVERED);
        eventRepository.save(event);

        createDeliveryLog(event, result, DeliveryStatus.DELIVERED);
        deliverySuccessCounter.increment();
    }

    private void handleFailure(WebhookEvent event, DeliveryResult result) {
        log.warn("Event {} delivery failed: {}", event.getEventId(), result.getErrorMessage());

        event.incrementRetryCount();
        event.setDeliveryStatus(DeliveryStatus.RETRYING);

        if (retryPolicy.shouldRetry(event.getRetryCount())) {
            int retryDelayMinutes = retryPolicy.getDelayMinutes(event.getRetryCount());
            event.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
            eventRepository.save(event);

            createDeliveryLog(event, result, DeliveryStatus.RETRYING);
            eventPublisher.publishForRetry(event, event.getRetryCount());
            deliveryRetryCounter.increment();

            log.info("Event {} scheduled for retry #{} in {} minutes",
                    event.getEventId(), event.getRetryCount(), retryDelayMinutes);
        } else {
            event.setEventStatus(EventStatus.FAILED);
            event.setDeliveryStatus(DeliveryStatus.FAILED);
            eventRepository.save(event);

            createDeliveryLog(event, result, DeliveryStatus.FAILED);
            eventPublisher.publishToDLQ(event);
            deliveryFailureCounter.increment();

            log.warn("Event {} moved to DLQ after {} attempts",
                    event.getEventId(), event.getRetryCount());
        }
    }

    private void handleProcessingError(WebhookEvent event, Exception e) {
        try {
            WebhookEvent freshEvent = eventRepository.findByIdWithEndpoint(event.getId())
                    .orElse(event);

            if (retryPolicy.shouldRetry(freshEvent.getRetryCount())) {
                freshEvent.incrementRetryCount();
                freshEvent.setDeliveryStatus(DeliveryStatus.RETRYING);

                int retryDelayMinutes = retryPolicy.getDelayMinutes(freshEvent.getRetryCount());
                freshEvent.setNextRetryAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                eventRepository.save(freshEvent);

                eventPublisher.publishForRetry(freshEvent, freshEvent.getRetryCount());
            } else {
                freshEvent.setEventStatus(EventStatus.FAILED);
                freshEvent.setDeliveryStatus(DeliveryStatus.FAILED);
                eventRepository.save(freshEvent);

                eventPublisher.publishToDLQ(freshEvent);
            }
        } catch (Exception ex) {
            log.error("Failed to handle processing error for event {}: {}",
                    event.getEventId(), ex.getMessage());
        }
    }

    private void createDeliveryLog(WebhookEvent event, DeliveryResult result, DeliveryStatus status) {
        DeliveryLog logEntry = DeliveryLog.builder()
                .event(event)
                .attemptNumber(event.getRetryCount() + 1)
                .status(status)
                .httpStatusCode(result.getHttpStatusCode())
                .responseBody(result.getResponseBody())
                .errorMessage(result.getErrorMessage())
                .requestTimestamp(result.getRequestTimestamp())
                .responseTimestamp(result.getResponseTimestamp())
                .durationMs(result.getDurationMs())
                .build();

        deliveryLogRepository.save(logEntry);
    }

    /**
     * Result of a delivery attempt.
     */
    public record DeliveryResult(
            boolean success,
            Integer httpStatusCode,
            String responseBody,
            String errorMessage,
            LocalDateTime requestTimestamp,
            LocalDateTime responseTimestamp,
            Long durationMs
    ) {
        public static DeliveryResult success(Integer httpStatusCode, String responseBody,
                                             LocalDateTime requestTime, LocalDateTime responseTime,
                                             Long duration) {
            return new DeliveryResult(true, httpStatusCode, responseBody, null,
                    requestTime, responseTime, duration);
        }

        public static DeliveryResult failure(String errorMessage, Integer httpStatusCode,
                                             LocalDateTime requestTime, LocalDateTime responseTime,
                                             Long duration) {
            return new DeliveryResult(false, httpStatusCode, null, errorMessage,
                    requestTime, responseTime, duration);
        }

        public boolean isSuccess() {
            return success;
        }

        public Integer getHttpStatusCode() {
            return httpStatusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public LocalDateTime getRequestTimestamp() {
            return requestTimestamp;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public LocalDateTime getResponseTimestamp() {
            return responseTimestamp;
        }

        public Long getDurationMs() {
            return durationMs;
        }
    }
}
