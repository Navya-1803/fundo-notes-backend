package com.fundoo.notes.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReminderEventTest {

    @Test
    void testConstructorAndGetters() {
        LocalDateTime time = LocalDateTime.of(2030, 1, 1, 9, 0);
        ReminderEvent event = new ReminderEvent(1L, 2L, "Pay bills", time, "Due today");
        assertEquals(1L, event.getNoteId());
        assertEquals(2L, event.getUserId());
        assertEquals("Pay bills", event.getNoteTitle());
        assertEquals(time, event.getReminderTime());
        assertEquals("Due today", event.getMessage());
        assertTrue(event.toString().contains("noteId=1"));
    }

    @Test
    void testSetters() {
        ReminderEvent event = new ReminderEvent();
        event.setNoteId(3L);
        event.setUserId(4L);
        event.setNoteTitle("T");
        event.setReminderTime(null);
        event.setMessage("M");
        assertEquals(3L, event.getNoteId());
        assertEquals(4L, event.getUserId());
        assertEquals("T", event.getNoteTitle());
        assertEquals("M", event.getMessage());
    }
}
