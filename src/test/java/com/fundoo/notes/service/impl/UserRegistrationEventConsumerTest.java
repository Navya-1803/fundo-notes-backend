package com.fundoo.notes.service.impl;

import com.fundoo.notes.config.ObjectMapperConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class UserRegistrationEventConsumerTest {

    private final UserRegistrationEventConsumer consumer = new UserRegistrationEventConsumer(new ObjectMapperConfig().eventObjectMapper());

    @Test
    void testHandleRegistration_SendsWelcomeEmail(CapturedOutput output) {
        consumer.handleRegistration("{\"userId\":1,\"email\":\"navya@example.com\",\"firstName\":\"Navya\"}");
        assertTrue(output.getOut().contains("Sending welcome email to navya@example.com"));
    }

    @Test
    void testHandleRegistration_InvalidMessage_IsIgnored(CapturedOutput output) {
        assertDoesNotThrow(() -> consumer.handleRegistration("not json"));
        assertTrue(output.getOut().contains("Ignoring invalid registration message"));
    }
}
