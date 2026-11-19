package com.fundoo.notes.dto.response;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NoteResponseDtoTest {

    @Test
    void testConstructor_SetsFields() {
        LocalDateTime now = LocalDateTime.now();
        NoteResponseDto dto = new NoteResponseDto(1L, "Title", "Body", true, false, false, now, now);
        assertEquals(1L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Body", dto.getDescription());
        assertTrue(dto.isPinned());
        assertFalse(dto.isArchived());
        assertFalse(dto.isTrashed());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    void testSetters() {
        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(2L);
        dto.setTitle("A");
        dto.setDescription("B");
        dto.setPinned(true);
        dto.setArchived(true);
        dto.setTrashed(true);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        assertEquals(2L, dto.getId());
        assertTrue(dto.isPinned() && dto.isArchived() && dto.isTrashed());
        assertEquals("A", dto.getTitle());
        assertEquals("B", dto.getDescription());
    }

    @Test
    void testIsSerializable() {
        assertInstanceOf(Serializable.class, new NoteResponseDto());
    }
}
