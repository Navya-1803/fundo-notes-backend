package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private RabbitMQConfig config() {
        RabbitMQConfig config = new RabbitMQConfig();
        ReflectionTestUtils.setField(config, "reminderQueue", "reminder.queue");
        ReflectionTestUtils.setField(config, "reminderExchange", "notes.exchange");
        ReflectionTestUtils.setField(config, "reminderRoutingKey", "reminder.created");
        return config;
    }

    @Test
    void testQueueExchangeAndBinding() {
        RabbitMQConfig config = config();
        Queue queue = config.reminderQueue();
        DirectExchange exchange = config.notesExchange();
        Binding binding = config.reminderBinding(queue, exchange);
        assertEquals("reminder.queue", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals("notes.exchange", exchange.getName());
        assertEquals("reminder.created", binding.getRoutingKey());
        assertEquals("reminder.queue", binding.getDestination());
    }

    @Test
    void testMessageConverter_IsJson() {
        assertInstanceOf(JacksonJsonMessageConverter.class, config().rabbitMessageConverter());
    }
}
