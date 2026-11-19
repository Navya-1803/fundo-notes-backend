package com.fundoo.notes.config;

import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JmsConfigTest {

    @Test
    void testListenerContainerFactory_IsCreated() {
        DefaultJmsListenerContainerFactory factory = new JmsConfig().jmsListenerContainerFactory(mock(ConnectionFactory.class), false);
        assertNotNull(factory);
    }
}
