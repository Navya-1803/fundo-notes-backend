package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteRequestDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void testValidNote() {
        assertTrue(validator.validate(new NoteRequestDto("Shopping", "Milk")).isEmpty());
        assertTrue(validator.validate(new NoteRequestDto("Title only", null)).isEmpty());
    }

    @Test
    void testBlankTitle_Rejected() {
        assertEquals("Title is required", validator.validate(new NoteRequestDto(" ", "x")).iterator().next().getMessage());
    }

    @Test
    void testTooLongValues_Rejected() {
        assertFalse(validator.validate(new NoteRequestDto("a".repeat(101), "x")).isEmpty());
        assertFalse(validator.validate(new NoteRequestDto("ok", "a".repeat(2001))).isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        NoteRequestDto dto = new NoteRequestDto();
        dto.setTitle("T");
        dto.setDescription("D");
        assertEquals("T", dto.getTitle());
        assertEquals("D", dto.getDescription());
    }
}
