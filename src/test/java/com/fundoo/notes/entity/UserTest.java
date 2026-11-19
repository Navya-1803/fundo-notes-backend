package com.fundoo.notes.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testConstructor_SetsFields() {
        User user = new User("Navya", "Raj", "navya@example.com", "secret");
        assertEquals("Navya", user.getFirstName());
        assertEquals("Raj", user.getLastName());
        assertEquals("navya@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertNull(user.getId());
    }

    @Test
    void testSetters() {
        User user = new User();
        user.setId(5L);
        user.setFirstName("Asha");
        user.setLastName("K");
        user.setEmail("asha@example.com");
        user.setPassword("pwd");
        assertEquals(5L, user.getId());
        assertEquals("Asha", user.getFirstName());
        assertEquals("K", user.getLastName());
        assertEquals("asha@example.com", user.getEmail());
        assertEquals("pwd", user.getPassword());
    }

    @Test
    void testLifecycleCallbacks_SetTimestamps() {
        User user = new User();
        user.onCreate();
        assertNotNull(user.getCreatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
        user.onUpdate();
        assertFalse(user.getUpdatedAt().isBefore(user.getCreatedAt()));
    }

    @Test
    void testToString_HidesPassword() {
        String text = new User("Navya", "Raj", "navya@example.com", "secret").toString();
        assertTrue(text.contains("navya@example.com"));
        assertFalse(text.contains("secret"));
    }
}
