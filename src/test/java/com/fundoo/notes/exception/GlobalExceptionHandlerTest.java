package com.fundoo.notes.exception;

import com.fundoo.notes.dto.response.ApiErrorResponseDto;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/register");

    @Test
    void testDuplicateEmail_Returns409() {
        ResponseEntity<ApiErrorResponseDto> response = handler.handleDuplicateEmail(new DuplicateEmailException("Email already registered"), request);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("Email already registered", response.getBody().getMessage());
        assertEquals("/api/users/register", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testInvalidCredentials_Returns401() {
        assertEquals(401, handler.handleInvalidCredentials(new InvalidCredentialsException("bad"), request).getStatusCode().value());
    }

    @Test
    void testIllegalArgument_Returns400() {
        assertEquals(400, handler.handleIllegalArgument(new IllegalArgumentException("bad input"), request).getStatusCode().value());
    }

    @Test
    void testGenericException_HidesInternalMessage() {
        ResponseEntity<ApiErrorResponseDto> response = handler.handleGeneric(new RuntimeException("NullPointer in line 42"), request);
        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().getMessage().contains("line 42"));
    }
}
