package com.fundoo.notes.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message published to RabbitMQ when a reminder is created on a note.
 *
 * @author Navya Raj
 */
public class ReminderEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long noteId;
    private Long userId;
    private String noteTitle;
    private LocalDateTime reminderTime;
    private String message;

    public ReminderEvent() {
    }

    public ReminderEvent(Long noteId, Long userId, String noteTitle, LocalDateTime reminderTime, String message) {
        this.noteId = noteId;
        this.userId = userId;
        this.noteTitle = noteTitle;
        this.reminderTime = reminderTime;
        this.message = message;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
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

    @Override
    public String toString() {
        return "ReminderEvent{noteId=" + noteId + ", userId=" + userId + ", reminderTime=" + reminderTime + "}";
    }
}
