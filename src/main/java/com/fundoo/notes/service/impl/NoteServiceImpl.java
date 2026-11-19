package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.NoteService;
import com.fundoo.notes.service.TokenService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Creates notes and moves them between pinned, archived and trashed states.
 *
 * @author Navya Raj
 */
@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository, TokenService tokenService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public NoteResponseDto createNote(NoteRequestDto requestDto, String token) {
        User user = findUserFromToken(token);
        Note note = new Note(requestDto.getTitle().trim(), requestDto.getDescription(), user);
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getAllNotes(String token) {
        Long userId = findUserFromToken(token).getId();
        // pinned notes first, then the most recently created
        return noteRepository.findByUserIdAndArchivedFalseAndTrashedFalse(userId).stream()
                .sorted(Comparator.comparing(Note::isPinned).reversed()
                        .thenComparing(Note::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public NoteResponseDto pinNote(Long noteId, String token) {
        Note note = findOwnedNote(noteId, token);
        if (note.isTrashed()) {
            throw new IllegalArgumentException("A note in trash cannot be pinned");
        }
        note.setPinned(!note.isPinned());
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    public NoteResponseDto archiveNote(Long noteId, String token) {
        Note note = findOwnedNote(noteId, token);
        if (note.isTrashed()) {
            throw new IllegalArgumentException("A note in trash cannot be archived");
        }
        note.setArchived(!note.isArchived());
        // archived notes are not shown as pinned on the dashboard
        if (note.isArchived()) {
            note.setPinned(false);
        }
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    public NoteResponseDto trashNote(Long noteId, String token) {
        Note note = findOwnedNote(noteId, token);
        note.setTrashed(!note.isTrashed());
        if (note.isTrashed()) {
            note.setPinned(false);
        }
        return mapToResponse(noteRepository.save(note));
    }

    // resolves the logged in user from the JWT
    private User findUserFromToken(String token) {
        Long userId = tokenService.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User of this token no longer exists"));
    }

    // a note owned by somebody else is reported as not found
    private Note findOwnedNote(Long noteId, String token) {
        Long userId = findUserFromToken(token).getId();
        return noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    private NoteResponseDto mapToResponse(Note note) {
        return new NoteResponseDto(note.getId(), note.getTitle(), note.getDescription(), note.isPinned(),
                note.isArchived(), note.isTrashed(), note.getCreatedAt(), note.getUpdatedAt());
    }
}
