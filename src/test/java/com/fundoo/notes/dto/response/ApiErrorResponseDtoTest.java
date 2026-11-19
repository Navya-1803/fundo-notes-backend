package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseDtoTest {

    @Test
    void testConstructor_FillsTimestamp() {
        ApiErrorResponseDto dto = new ApiErrorResponseDto(404, "Not Found", "Note not found", "/api/notes/1/pin");
        assertNotNull(dto.getTimestamp());
        assertEquals(404, dto.getStatus());
        assertEquals("Not Found", dto.getError());
        assertEquals("Note not found", dto.getMessage());
        assertEquals("/api/notes/1/pin", dto.getPath());
        assertNull(dto.getValidationErrors());
    }

    @Test
    void testSetters() {
        ApiErrorResponseDto dto = new ApiErrorResponseDto();
        dto.setStatus(400);
        dto.setError("Bad Request");
        dto.setMessage("Validation failed");
        dto.setPath("/api/users/register");
        dto.setTimestamp(null);
        dto.setValidationErrors(Map.of("email", "Enter a valid email"));
        assertEquals(400, dto.getStatus());
        assertEquals("Bad Request", dto.getError());
        assertEquals("Validation failed", dto.getMessage());
        assertEquals("/api/users/register", dto.getPath());
        assertEquals("Enter a valid email", dto.getValidationErrors().get("email"));
    }
}
