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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

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
        log.info("Creating note for user id: {}", user.getId());
        log.debug("Note title: {}", requestDto.getTitle());
        Note savedNote = noteRepository.save(new Note(requestDto.getTitle().trim(), requestDto.getDescription(), user));
        log.info("Note created with id: {}", savedNote.getId());
        return mapToResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponseDto> getAllNotes(String token) {
        Long userId = findUserFromToken(token).getId();
        log.info("Fetching notes for user id: {}", userId);
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
            log.warn("Pin rejected, note id: {} is in trash", noteId);
            throw new IllegalArgumentException("A note in trash cannot be pinned");
        }
        note.setPinned(!note.isPinned());
        log.info("Note id: {} pinned: {}", noteId, note.isPinned());
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    public NoteResponseDto archiveNote(Long noteId, String token) {
        Note note = findOwnedNote(noteId, token);
        if (note.isTrashed()) {
            log.warn("Archive rejected, note id: {} is in trash", noteId);
            throw new IllegalArgumentException("A note in trash cannot be archived");
        }
        note.setArchived(!note.isArchived());
        log.info("Note id: {} archived: {}", noteId, note.isArchived());
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
        log.info("Note id: {} trashed: {}", noteId, note.isTrashed());
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
        return noteRepository.findByIdAndUserId(noteId, userId).orElseThrow(() -> {
            log.warn("Note id: {} not found for user id: {}", noteId, userId);
            return new NoteNotFoundException(noteId);
        });
    }

    private NoteResponseDto mapToResponse(Note note) {
        return new NoteResponseDto(note.getId(), note.getTitle(), note.getDescription(), note.isPinned(),
                note.isArchived(), note.isTrashed(), note.getCreatedAt(), note.getUpdatedAt());
    }
}
