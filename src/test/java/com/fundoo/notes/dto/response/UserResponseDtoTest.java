package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDtoTest {

    @Test
    void testConstructor_SetsFields() {
        LocalDateTime now = LocalDateTime.now();
        UserResponseDto dto = new UserResponseDto(1L, "Navya", "Raj", "navya@example.com", now);
        assertEquals(1L, dto.getId());
        assertEquals("Navya", dto.getFirstName());
        assertEquals("Raj", dto.getLastName());
        assertEquals("navya@example.com", dto.getEmail());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testSetters() {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(2L);
        dto.setFirstName("Asha");
        dto.setLastName("K");
        dto.setEmail("asha@example.com");
        dto.setCreatedAt(null);
        assertEquals(2L, dto.getId());
        assertEquals("Asha", dto.getFirstName());
        assertEquals("K", dto.getLastName());
        assertEquals("asha@example.com", dto.getEmail());
        assertNull(dto.getCreatedAt());
    }

    @Test
    void testHasNoPasswordField() {
        assertThrows(NoSuchFieldException.class, () -> UserResponseDto.class.getDeclaredField("password"));
    }
}
