package com.fundoo.notes.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationEventTest {

    @Test
    void testConstructor_FillsRegisteredAt() {
        UserRegistrationEvent event = new UserRegistrationEvent(1L, "navya@example.com", "Navya");
        assertEquals(1L, event.getUserId());
        assertEquals("navya@example.com", event.getEmail());
        assertEquals("Navya", event.getFirstName());
        assertNotNull(event.getRegisteredAt());
    }

    @Test
    void testSetters() {
        UserRegistrationEvent event = new UserRegistrationEvent();
        event.setUserId(2L);
        event.setEmail("a@example.com");
        event.setFirstName("A");
        event.setRegisteredAt("2026-01-01T10:00");
        assertEquals(2L, event.getUserId());
        assertEquals("a@example.com", event.getEmail());
        assertEquals("A", event.getFirstName());
        assertEquals("2026-01-01T10:00", event.getRegisteredAt());
        assertTrue(event.toString().contains("a@example.com"));
    }
}
