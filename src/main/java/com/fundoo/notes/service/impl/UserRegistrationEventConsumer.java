package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.UserRegistrationEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Listens to the registration queue and does the slow follow-up work
 * (sending the welcome email) outside the HTTP request.
 *
 * @author Navya Raj
 */
// listeners can be switched off, for example in tests where no broker is running
@Component
@ConditionalOnProperty(name = "app.messaging.listeners-enabled", havingValue = "true", matchIfMissing = true)
public class UserRegistrationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationEventConsumer.class);

    private final ObjectMapper objectMapper;

    public UserRegistrationEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${app.jms.user-registration-queue}")
    public void handleRegistration(String message) {
        try {
            UserRegistrationEvent event = objectMapper.readValue(message, UserRegistrationEvent.class);
            log.info("Received registration event: {}", event);
            sendWelcomeEmail(event);
        } catch (JsonProcessingException e) {
            log.error("Ignoring invalid registration message: {}", message);
        }
    }

    // a real mail sender can be plugged in here, for now the mail is logged
    void sendWelcomeEmail(UserRegistrationEvent event) {
        log.info("Sending welcome email to {} ({})", event.getEmail(), event.getFirstName());
    }
}
