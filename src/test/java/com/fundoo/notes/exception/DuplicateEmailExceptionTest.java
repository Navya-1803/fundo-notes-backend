package com.fundoo.notes.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateEmailExceptionTest {

    @Test
    void testMessageAndType() {
        DuplicateEmailException exception = new DuplicateEmailException("Email already registered");
        assertEquals("Email already registered", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }
}
