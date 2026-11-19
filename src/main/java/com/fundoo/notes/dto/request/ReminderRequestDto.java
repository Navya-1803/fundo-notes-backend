package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request body used to set a reminder on a note.
 *
 * @author Navya Raj
 */
public class ReminderRequestDto {

    @NotNull(message = "Note id is required")
    private Long noteId;

    @NotNull(message = "Reminder time is required")
    @Future(message = "Reminder time must be in the future")
    private LocalDateTime reminderTime;

    @Size(max = 255, message = "Message must be at most 255 characters")
    private String message;

    public ReminderRequestDto() {
    }

    public ReminderRequestDto(Long noteId, LocalDateTime reminderTime, String message) {
        this.noteId = noteId;
        this.reminderTime = reminderTime;
        this.message = message;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
