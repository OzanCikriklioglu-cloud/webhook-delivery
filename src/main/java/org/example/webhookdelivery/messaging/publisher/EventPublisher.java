package org.example.webhookdelivery.messaging.publisher;

import org.example.webhookdelivery.config.RabbitMQConfig;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish a webhook event to the delivery queue for immediate processing.
     */
    public void publishForDelivery(WebhookEvent event) {
        log.info("Publishing event {} to delivery queue", event.getEventId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WEBHOOK_EXCHANGE,
                RabbitMQConfig.DELIVERY_ROUTING_KEY,
                event
        );
        log.debug("Event {} published successfully", event.getEventId());
    }

    /**
     * Publish an event to the appropriate retry queue based on retry count.
     * Retry 1 → 1m queue, Retry 2 → 5m queue, Retry 3+ → 30m queue.
     */
    public void publishForRetry(WebhookEvent event, int retryCount) {
        String routingKey = switch (retryCount) {
            case 1  -> RabbitMQConfig.RETRY_1M_ROUTING_KEY;
            case 2  -> RabbitMQConfig.RETRY_5M_ROUTING_KEY;
            default -> RabbitMQConfig.RETRY_30M_ROUTING_KEY;
        };
        log.info("Publishing event {} to retry queue (attempt {}, delay routing key: {})",
                event.getEventId(), retryCount, routingKey);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WEBHOOK_EXCHANGE,
                routingKey,
                event
        );
    }

    /**
     * Publish an event directly to the dead letter queue.
     */
    public void publishToDLQ(WebhookEvent event) {
        log.warn("Publishing event {} to DLQ after {} attempts",
                event.getEventId(), event.getRetryCount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DLX_EXCHANGE,
                RabbitMQConfig.DLQ_ROUTING_KEY,
                event
        );
    }
}
