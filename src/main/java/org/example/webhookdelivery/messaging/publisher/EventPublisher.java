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
     * Publish an event to the retry queue for delayed retry processing.
     */
    public void publishForRetry(WebhookEvent event) {
        log.info("Publishing event {} to retry queue (attempt {})",
                event.getEventId(), event.getRetryCount());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WEBHOOK_EXCHANGE,
                RabbitMQConfig.RETRY_ROUTING_KEY,
                event
        );
        log.debug("Event {} published to retry queue", event.getEventId());
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
