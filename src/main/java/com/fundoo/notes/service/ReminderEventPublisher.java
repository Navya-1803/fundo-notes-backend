package com.fundoo.notes.service;

import com.fundoo.notes.event.ReminderEvent;

/**
 * Publishes reminder events for asynchronous notification.
 *
 * @author Navya Raj
 */
public interface ReminderEventPublisher {

    void publish(ReminderEvent event);
}
