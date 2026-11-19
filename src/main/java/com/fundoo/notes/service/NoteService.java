package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;

import java.util.List;

/**
 * Business operations on notes. Every method receives the caller's token
 * and only works on notes owned by that user.
 *
 * @author Navya Raj
 */
public interface NoteService {

    NoteResponseDto createNote(NoteRequestDto requestDto, String token);

    List<NoteResponseDto> getAllNotes(String token);

    NoteResponseDto pinNote(Long noteId, String token);

    NoteResponseDto archiveNote(Long noteId, String token);

    NoteResponseDto trashNote(Long noteId, String token);
}
