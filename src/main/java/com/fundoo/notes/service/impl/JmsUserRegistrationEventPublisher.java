package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.UserRegistrationEvent;
import com.fundoo.notes.service.UserRegistrationEventPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * Sends registration events as JSON to the ActiveMQ queue. Publishing is
 * best effort: if the broker is down the registration still succeeds and
 * the problem is only logged.
 *
 * @author Navya Raj
 */
@Service
public class JmsUserRegistrationEventPublisher implements UserRegistrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(JmsUserRegistrationEventPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public JmsUserRegistrationEventPublisher(JmsTemplate jmsTemplate, ObjectMapper objectMapper,
                                             @Value("${app.jms.user-registration-queue}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    @Override
    public void publish(UserRegistrationEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend(queueName, message);
            log.info("Published registration event for user id: {} to queue: {}", event.getUserId(), queueName);
        } catch (JsonProcessingException | JmsException e) {
            log.error("Could not publish registration event for user id: {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
