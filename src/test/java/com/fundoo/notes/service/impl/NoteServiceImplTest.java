package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class NoteServiceImplTest {

    private static final String TOKEN = "Bearer token";

    private NoteRepository noteRepository;
    private UserRepository userRepository;
    private TokenService tokenService;
    private NoteServiceImpl noteService;
    private User user;

    @BeforeEach
    void setUp() {
        noteRepository = mock(NoteRepository.class);
        userRepository = mock(UserRepository.class);
        tokenService = mock(TokenService.class);
        noteService = new NoteServiceImpl(noteRepository, userRepository, tokenService);

        user = new User("Navya", "Raj", "navya@example.com", "hash");
        user.setId(1L);
        when(tokenService.extractUserId(TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Note note(Long id) {
        Note note = new Note("Title " + id, "Body", user);
        note.setId(id);
        return note;
    }

    @Test
    void testCreateNote_SavesForTokenUser() {
        NoteResponseDto response = noteService.createNote(new NoteRequestDto("  Shopping ", "Milk"), TOKEN);
        assertEquals("Shopping", response.getTitle());
        verify(noteRepository).save(argThatOwnedBy(user));
    }

    private Note argThatOwnedBy(User owner) {
        return org.mockito.ArgumentMatchers.argThat(n -> n.getUser() == owner);
    }

    @Test
    void testGetAllNotes_PinnedFirst() {
        Note normal = note(1L);
        Note pinned = note(2L);
        pinned.setPinned(true);
        when(noteRepository.findByUserIdAndArchivedFalseAndTrashedFalse(1L)).thenReturn(List.of(normal, pinned));
        List<NoteResponseDto> notes = noteService.getAllNotes(TOKEN);
        assertEquals(2L, notes.get(0).getId());
        assertEquals(1L, notes.get(1).getId());
    }

    @Test
    void testPinNote_Toggles() {
        Note note = note(5L);
        when(noteRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(note));
        assertTrue(noteService.pinNote(5L, TOKEN).isPinned());
        assertFalse(noteService.pinNote(5L, TOKEN).isPinned());
    }

    @Test
    void testPinNote_TrashedNoteRejected() {
        Note note = note(5L);
        note.setTrashed(true);
        when(noteRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(note));
        assertThrows(IllegalArgumentException.class, () -> noteService.pinNote(5L, TOKEN));
    }

    @Test
    void testArchiveNote_UnpinsNote() {
        Note note = note(5L);
        note.setPinned(true);
        when(noteRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(note));
        NoteResponseDto response = noteService.archiveNote(5L, TOKEN);
        assertTrue(response.isArchived());
        assertFalse(response.isPinned());
    }

    @Test
    void testTrashNote_TogglesAndRestores() {
        Note note = note(5L);
        when(noteRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(note));
        assertTrue(noteService.trashNote(5L, TOKEN).isTrashed());
        assertFalse(noteService.trashNote(5L, TOKEN).isTrashed());
    }

    @Test
    void testNoteOfOtherUser_NotFound() {
        when(noteRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.archiveNote(9L, TOKEN));
    }

    @Test
    void testDeletedUser_Rejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> noteService.getAllNotes(TOKEN));
    }

    @Test
    void testNoteNotFound_LogsWarning(CapturedOutput output) {
        when(noteRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.trashNote(9L, TOKEN));
        assertTrue(output.getOut().contains("Note id: 9 not found for user id: 1"));
    }
}
