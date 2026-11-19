package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User navya;
    private User other;

    @BeforeEach
    void setUp() {
        navya = userRepository.save(new User("Navya", "Raj", "navya@example.com", "hash"));
        other = userRepository.save(new User("Other", "User", "other@example.com", "hash"));

        noteRepository.save(new Note("Shopping list", "milk", navya));
        Note archived = new Note("Old ideas", "", navya);
        archived.setArchived(true);
        noteRepository.save(archived);
        Note trashed = new Note("Deleted", "", navya);
        trashed.setTrashed(true);
        noteRepository.save(trashed);
        noteRepository.save(new Note("Other user's note", "", other));
    }

    @Test
    void testFindByUserId() {
        assertEquals(3, noteRepository.findByUserId(navya.getId()).size());
        assertEquals(1, noteRepository.findByUserId(other.getId()).size());
    }

    @Test
    void testFindActiveNotes() {
        assertEquals(1, noteRepository.findByUserIdAndArchivedFalseAndTrashedFalse(navya.getId()).size());
    }

    @Test
    void testFindArchivedAndTrashedNotes() {
        assertEquals(1, noteRepository.findByUserIdAndArchivedTrueAndTrashedFalse(navya.getId()).size());
        assertEquals(1, noteRepository.findByUserIdAndTrashedTrue(navya.getId()).size());
    }

    @Test
    void testSearchByTitle_IgnoresCase() {
        assertEquals(1, noteRepository.findByUserIdAndTitleContainingIgnoreCase(navya.getId(), "SHOPPING").size());
        assertTrue(noteRepository.findByUserIdAndTitleContainingIgnoreCase(navya.getId(), "other").isEmpty());
    }

    @Test
    void testFindByIdAndUserId_OnlyOwnerCanSeeNote() {
        Note note = noteRepository.findByUserId(navya.getId()).get(0);
        assertTrue(noteRepository.findByIdAndUserId(note.getId(), navya.getId()).isPresent());
        assertTrue(noteRepository.findByIdAndUserId(note.getId(), other.getId()).isEmpty());
    }
}
