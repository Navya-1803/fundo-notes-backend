package com.fundoo.notes.service.impl;

import com.fundoo.notes.event.ReminderEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class RabbitMQReminderConsumerTest {

    private final RabbitMQReminderConsumer consumer = new RabbitMQReminderConsumer();

    @Test
    void testConsumeReminder_UsesMessage(CapturedOutput output) {
        consumer.consumeReminder(new ReminderEvent(1L, 2L, "Pay bills", LocalDateTime.of(2030, 1, 1, 9, 0), "Due today"));
        assertTrue(output.getOut().contains("Reminder for user id: 2 at 2030-01-01T09:00: Due today"));
    }

    @Test
    void testConsumeReminder_FallsBackToTitle(CapturedOutput output) {
        consumer.consumeReminder(new ReminderEvent(1L, 2L, "Pay bills", LocalDateTime.of(2030, 1, 1, 9, 0), " "));
        assertTrue(output.getOut().contains(": Pay bills"));
    }
}
