package com.fundoo.notes.exception;

/**
 * Thrown when a note does not exist or does not belong to the current user.
 *
 * @author Navya Raj
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long noteId) {
        super("Note not found with id: " + noteId);
    }
}
