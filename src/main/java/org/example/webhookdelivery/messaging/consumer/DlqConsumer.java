package org.example.webhookdelivery.messaging.consumer;

import org.example.webhookdelivery.config.RabbitMQConfig;
import org.example.webhookdelivery.domain.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens on the Dead Letter Queue and logs final failures.
 * By the time a message reaches here, the event is already marked FAILED in the DB
 * by DeliveryConsumer. This consumer is a monitoring safety net.
 */
@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDlq(WebhookEvent event) {
        log.error("Event {} landed in DLQ — eventType: {}, retryCount: {}, endpointId: {}",
                event.getEventId(),
                event.getEventType(),
                event.getRetryCount(),
                event.getEndpoint() != null ? event.getEndpoint().getId() : "unknown"
        );
    }
}
