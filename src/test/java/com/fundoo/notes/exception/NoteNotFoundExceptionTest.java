package com.fundoo.notes.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteNotFoundExceptionTest {

    @Test
    void testMessageContainsId() {
        assertEquals("Note not found with id: 15", new NoteNotFoundException(15L).getMessage());
    }
}
