package com.fundoo.notes.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void testMessage() {
        assertEquals("Invalid email or password", new InvalidCredentialsException("Invalid email or password").getMessage());
    }

    @Test
    void testCause() {
        IllegalStateException cause = new IllegalStateException("expired");
        assertSame(cause, new InvalidCredentialsException("Invalid token", cause).getCause());
    }
}
