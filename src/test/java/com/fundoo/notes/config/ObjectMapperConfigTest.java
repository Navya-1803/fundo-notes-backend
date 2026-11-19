package com.fundoo.notes.config;

import com.fundoo.notes.event.UserRegistrationEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapperConfig().eventObjectMapper();

    @Test
    void testRoundTrip() throws Exception {
        String json = objectMapper.writeValueAsString(new UserRegistrationEvent(1L, "navya@example.com", "Navya"));
        UserRegistrationEvent event = objectMapper.readValue(json, UserRegistrationEvent.class);
        assertEquals("navya@example.com", event.getEmail());
    }

    @Test
    void testUnknownFields_AreIgnored() throws Exception {
        UserRegistrationEvent event = objectMapper.readValue("{\"userId\":1,\"newField\":\"x\"}", UserRegistrationEvent.class);
        assertEquals(1L, event.getUserId());
    }
}
