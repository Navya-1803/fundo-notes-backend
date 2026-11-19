package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.NoteService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for notes. The JWT from login is sent in the
 * Authorization header of every request.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(@Valid @RequestBody NoteRequestDto requestDto,
                                                      @RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(requestDto, token));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(noteService.getAllNotes(token));
    }

    // pin, archive and trash toggle the current state of the note
    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<NoteResponseDto> pinNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(noteService.pinNote(noteId, token));
    }

    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<NoteResponseDto> archiveNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(noteService.archiveNote(noteId, token));
    }

    @PatchMapping("/{noteId}/trash")
    public ResponseEntity<NoteResponseDto> trashNote(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(noteService.trashNote(noteId, token));
    }
}
