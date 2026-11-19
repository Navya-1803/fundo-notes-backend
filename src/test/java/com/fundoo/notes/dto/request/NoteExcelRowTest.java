package com.fundoo.notes.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteExcelRowTest {

    @Test
    void testConstructorAndGetters() {
        NoteExcelRow row = new NoteExcelRow(2, "Title", "Body", "navya@example.com", true, false);
        assertEquals(2, row.getRowNumber());
        assertEquals("Title", row.getTitle());
        assertEquals("Body", row.getDescription());
        assertEquals("navya@example.com", row.getOwnerEmail());
        assertTrue(row.isPinned());
        assertFalse(row.isArchived());
        assertTrue(row.toString().contains("row=2"));
    }

    @Test
    void testSetters() {
        NoteExcelRow row = new NoteExcelRow();
        row.setRowNumber(5);
        row.setTitle("T");
        row.setDescription("D");
        row.setOwnerEmail("e@example.com");
        row.setPinned(true);
        row.setArchived(true);
        assertEquals(5, row.getRowNumber());
        assertEquals("T", row.getTitle());
        assertEquals("D", row.getDescription());
        assertEquals("e@example.com", row.getOwnerEmail());
        assertTrue(row.isPinned() && row.isArchived());
    }
}
