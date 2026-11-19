package com.fundoo.notes.service;

import com.fundoo.notes.event.UserRegistrationEvent;

/**
 * Publishes user registration events for background processing.
 *
 * @author Navya Raj
 */
public interface UserRegistrationEventPublisher {

    void publish(UserRegistrationEvent event);
}
