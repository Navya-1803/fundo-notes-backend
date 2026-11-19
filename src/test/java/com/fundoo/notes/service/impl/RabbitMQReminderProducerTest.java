package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RabbitMQReminderProducerTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final RabbitMQReminderProducer producer = new RabbitMQReminderProducer(rabbitTemplate, "notes.exchange", "reminder.created");
    private final ReminderEvent event = new ReminderEvent(1L, 2L, "Title", LocalDateTime.now().plusDays(1), "msg");

    @Test
    void testPublish_SendsToExchangeWithRoutingKey() {
        producer.publish(event);
        verify(rabbitTemplate).convertAndSend("notes.exchange", "reminder.created", event);
    }

    @Test
    void testPublish_BrokerDown_Throws() {
        doThrow(new AmqpConnectException(new RuntimeException("down"))).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));
        assertThrows(IllegalStateException.class, () -> producer.publish(event));
    }
}
