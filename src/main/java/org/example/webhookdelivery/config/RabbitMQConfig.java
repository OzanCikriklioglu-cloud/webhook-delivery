package org.example.webhookdelivery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String WEBHOOK_EXCHANGE = "webhook.exchange";
    public static final String DLX_EXCHANGE = "webhook.dlx.exchange";

    // Queue names
    public static final String DELIVERY_QUEUE = "webhook.delivery.queue";
    public static final String RETRY_QUEUE = "webhook.retry.queue";
    public static final String DLQ_QUEUE = "webhook.dlq.queue";

    // Routing keys
    public static final String DELIVERY_ROUTING_KEY = "webhook.deliver";
    public static final String RETRY_ROUTING_KEY = "webhook.retry";
    public static final String DLQ_ROUTING_KEY = "webhook.dlq";

    // Message TTL for retry queue (1 minute)
    public static final int RETRY_TTL = 60000;

    // ==================== Main Exchange ====================
    @Bean
    public DirectExchange webhookExchange() {
        return new DirectExchange(WEBHOOK_EXCHANGE, true, false);
    }

    // ==================== Dead Letter Exchange ====================
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ==================== Main Delivery Queue ====================
    @Bean
    public Queue deliveryQueue() {
        return QueueBuilder.durable(DELIVERY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    // ==================== Retry Queue (with TTL, redirects back to main queue) ====================
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", WEBHOOK_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DELIVERY_ROUTING_KEY)
                .withArgument("x-message-ttl", RETRY_TTL)
                .build();
    }

    // ==================== Dead Letter Queue (DLQ) ====================
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // ==================== Bindings ====================
    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue())
                .to(webhookExchange())
                .with(DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(webhookExchange())
                .with(RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue())
                .to(dlxExchange())
                .with(DLQ_ROUTING_KEY);
    }

    // ==================== Message Converter ====================
    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
    // ==================== RabbitTemplate with JSON converter ====================
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
