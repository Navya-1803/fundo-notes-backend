package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void testPasswordEncoder_IsBCrypt() {
        PasswordEncoder encoder = new PasswordConfig().passwordEncoder();
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
        assertTrue(encoder.encode("secret123").startsWith("$2"));
    }
}
