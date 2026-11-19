package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseDtoTest {

    @Test
    void testConstructor_SetsFields() {
        LoginResponseDto dto = new LoginResponseDto("token-value", "Login successful");
        assertEquals("token-value", dto.getToken());
        assertEquals("Login successful", dto.getMessage());
    }

    @Test
    void testSetters() {
        LoginResponseDto dto = new LoginResponseDto();
        dto.setToken("abc");
        dto.setMessage("ok");
        assertEquals("abc", dto.getToken());
        assertEquals("ok", dto.getMessage());
    }
}
