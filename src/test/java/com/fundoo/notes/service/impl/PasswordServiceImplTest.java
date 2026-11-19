package com.fundoo.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceImplTest {

    private final PasswordServiceImpl passwordService = new PasswordServiceImpl(new BCryptPasswordEncoder());

    @Test
    void testEncode_DoesNotReturnRawPassword() {
        String encoded = passwordService.encode("secret123");
        assertNotEquals("secret123", encoded);
        assertTrue(encoded.startsWith("$2"));
    }

    @Test
    void testEncode_SamePasswordGivesDifferentHashes() {
        // BCrypt uses a random salt for every hash
        assertNotEquals(passwordService.encode("secret123"), passwordService.encode("secret123"));
    }

    @Test
    void testMatches() {
        String encoded = passwordService.encode("secret123");
        assertTrue(passwordService.matches("secret123", encoded));
        assertFalse(passwordService.matches("wrong123", encoded));
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> passwordService.encode(" "));
        assertFalse(passwordService.matches(null, "hash"));
        assertFalse(passwordService.matches("secret123", null));
    }
}
