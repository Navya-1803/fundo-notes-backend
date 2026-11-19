package com.fundoo.notes.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDtoTest {

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
    void testValidRequest_NoViolations() {
        assertTrue(validator.validate(new LoginRequestDto("navya@example.com", "secret123")).isEmpty());
    }

    @Test
    void testMissingFields_Rejected() {
        assertEquals(2, validator.validate(new LoginRequestDto("", "")).size());
    }

    @Test
    void testInvalidEmail_Rejected() {
        assertFalse(validator.validate(new LoginRequestDto("wrong", "secret123")).isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("navya@example.com");
        dto.setPassword("secret123");
        assertEquals("navya@example.com", dto.getEmail());
        assertEquals("secret123", dto.getPassword());
    }
}
