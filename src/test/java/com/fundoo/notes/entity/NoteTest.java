package com.fundoo.notes.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteTest {

    @Test
    void testConstructor_DefaultStates() {
        User owner = new User("Navya", "Raj", "navya@example.com", "hash");
        Note note = new Note("Shopping", "Milk and bread", owner);
        assertEquals("Shopping", note.getTitle());
        assertEquals("Milk and bread", note.getDescription());
        assertSame(owner, note.getUser());
        assertFalse(note.isPinned());
        assertFalse(note.isArchived());
        assertFalse(note.isTrashed());
    }

    @Test
    void testSetters() {
        Note note = new Note();
        note.setId(3L);
        note.setTitle("Title");
        note.setDescription("Body");
        note.setPinned(true);
        note.setArchived(true);
        note.setTrashed(true);
        User owner = new User();
        note.setUser(owner);
        assertEquals(3L, note.getId());
        assertEquals("Title", note.getTitle());
        assertEquals("Body", note.getDescription());
        assertTrue(note.isPinned() && note.isArchived() && note.isTrashed());
        assertSame(owner, note.getUser());
    }

    @Test
    void testLifecycleCallbacks_SetTimestamps() {
        Note note = new Note();
        note.onCreate();
        assertNotNull(note.getCreatedAt());
        note.onUpdate();
        assertFalse(note.getUpdatedAt().isBefore(note.getCreatedAt()));
    }

    @Test
    void testToString() {
        assertTrue(new Note("Ideas", "", null).toString().contains("title='Ideas'"));
    }
}
