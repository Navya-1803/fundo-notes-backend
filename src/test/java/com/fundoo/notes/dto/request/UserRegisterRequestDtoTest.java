package com.fundoo.notes.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterRequestDtoTest {

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

    private Set<String> messages(UserRegisterRequestDto dto) {
        return validator.validate(dto).stream().map(ConstraintViolation::getMessage).collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void testValidRequest_NoViolations() {
        assertTrue(validator.validate(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123")).isEmpty());
    }

    @Test
    void testBlankFirstName_Rejected() {
        assertTrue(messages(new UserRegisterRequestDto("", "Raj", "navya@example.com", "secret123")).contains("First name is required"));
    }

    @Test
    void testInvalidEmail_Rejected() {
        assertTrue(messages(new UserRegisterRequestDto("Navya", "Raj", "not-an-email", "secret123")).contains("Enter a valid email"));
    }

    @Test
    void testWeakPassword_Rejected() {
        assertFalse(validator.validate(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "short")).isEmpty());
        assertFalse(validator.validate(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "onlyletters")).isEmpty());
    }

    @Test
    void testLastNameIsOptional() {
        assertTrue(validator.validate(new UserRegisterRequestDto("Navya", null, "navya@example.com", "secret123")).isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto();
        dto.setFirstName("Navya");
        dto.setLastName("Raj");
        dto.setEmail("navya@example.com");
        dto.setPassword("secret123");
        assertEquals("Navya", dto.getFirstName());
        assertEquals("Raj", dto.getLastName());
        assertEquals("navya@example.com", dto.getEmail());
        assertEquals("secret123", dto.getPassword());
    }
}
