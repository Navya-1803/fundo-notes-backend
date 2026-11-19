package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.service.ReminderEventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publishes reminder events to the notes exchange in RabbitMQ.
 *
 * @author Navya Raj
 */
@Service
public class RabbitMQReminderProducer implements ReminderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQReminderProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitMQReminderProducer(RabbitTemplate rabbitTemplate,
                                    @Value("${app.rabbitmq.reminder-exchange}") String exchange,
                                    @Value("${app.rabbitmq.reminder-routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publish(ReminderEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published reminder event for note id: {} to exchange: {}", event.getNoteId(), exchange);
        } catch (AmqpException e) {
            // unlike the welcome email, the client must know that the reminder was not scheduled
            log.error("Could not publish reminder for note id: {}: {}", event.getNoteId(), e.getMessage());
            throw new IllegalStateException("Reminder service is currently unavailable", e);
        }
    }
}
