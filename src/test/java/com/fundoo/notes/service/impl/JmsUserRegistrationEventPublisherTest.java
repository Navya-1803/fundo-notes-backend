package com.fundoo.notes.service.impl;

import com.fundoo.notes.config.ObjectMapperConfig;
import com.fundoo.notes.event.UserRegistrationEvent;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JmsUserRegistrationEventPublisherTest {

    private final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    private final JmsUserRegistrationEventPublisher publisher =
            new JmsUserRegistrationEventPublisher(jmsTemplate, new ObjectMapperConfig().eventObjectMapper(), "user.registration.queue");

    @Test
    void testPublish_SendsJsonToQueue() {
        publisher.publish(new UserRegistrationEvent(1L, "navya@example.com", "Navya"));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(jmsTemplate).convertAndSend(eq("user.registration.queue"), captor.capture());
        assertTrue(captor.getValue().toString().contains("\"email\":\"navya@example.com\""));
    }

    @Test
    void testPublish_BrokerDown_DoesNotThrow() {
        doThrow(new UncategorizedJmsException("broker down")).when(jmsTemplate).convertAndSend(anyString(), any(Object.class));
        assertDoesNotThrow(() -> publisher.publish(new UserRegistrationEvent(1L, "navya@example.com", "Navya")));
    }
}
