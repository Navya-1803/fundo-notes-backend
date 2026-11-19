package com.fundoo.notes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the reminder queue, the notes exchange and the binding between
 * them. Messages published to the exchange with the reminder routing key
 * end up in the reminder queue.
 *
 * @author Navya Raj
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.reminder-queue}")
    private String reminderQueue;

    @Value("${app.rabbitmq.reminder-exchange}")
    private String reminderExchange;

    @Value("${app.rabbitmq.reminder-routing-key}")
    private String reminderRoutingKey;

    // durable so queued reminders survive a broker restart
    @Bean
    public Queue reminderQueue() {
        return new Queue(reminderQueue, true);
    }

    @Bean
    public DirectExchange notesExchange() {
        return new DirectExchange(reminderExchange);
    }

    @Bean
    public Binding reminderBinding(Queue reminderQueue, DirectExchange notesExchange) {
        return BindingBuilder.bind(reminderQueue).to(notesExchange).with(reminderRoutingKey);
    }

    // events are sent as JSON instead of Java serialization
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
