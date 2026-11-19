package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes reminder events from RabbitMQ and sends the notification.
 *
 * @author Navya Raj
 */
// listeners can be switched off, for example in tests where no broker is running
@Component
@ConditionalOnProperty(name = "app.messaging.listeners-enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQReminderConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQReminderConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.reminder-queue}")
    public void consumeReminder(ReminderEvent event) {
        log.info("Received reminder event: {}", event);
        sendNotification(event);
    }

    // a mail or push notification sender can be plugged in here, for now it is logged
    void sendNotification(ReminderEvent event) {
        String text = event.getMessage() == null || event.getMessage().isBlank() ? event.getNoteTitle() : event.getMessage();
        log.info("Reminder for user id: {} at {}: {}", event.getUserId(), event.getReminderTime(), text);
    }
}
