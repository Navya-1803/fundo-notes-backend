package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReminderRequestDtoTest {

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
    void testValidReminder() {
        assertTrue(validator.validate(new ReminderRequestDto(1L, LocalDateTime.now().plusHours(1), "Call")).isEmpty());
    }

    @Test
    void testPastTime_Rejected() {
        assertEquals("Reminder time must be in the future",
                validator.validate(new ReminderRequestDto(1L, LocalDateTime.now().minusHours(1), null)).iterator().next().getMessage());
    }

    @Test
    void testMissingFields_Rejected() {
        assertEquals(2, validator.validate(new ReminderRequestDto(null, null, null)).size());
    }

    @Test
    void testGettersAndSetters() {
        ReminderRequestDto dto = new ReminderRequestDto();
        LocalDateTime time = LocalDateTime.now();
        dto.setNoteId(4L);
        dto.setReminderTime(time);
        dto.setMessage("m");
        assertEquals(4L, dto.getNoteId());
        assertEquals(time, dto.getReminderTime());
        assertEquals("m", dto.getMessage());
    }
}
