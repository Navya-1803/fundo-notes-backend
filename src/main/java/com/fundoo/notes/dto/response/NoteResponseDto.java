package com.fundoo.notes.dto.response;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Note details returned to the client.
 *
 * @author Navya Raj
 */
public class NoteResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private boolean pinned;
    private boolean archived;
    private boolean trashed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoteResponseDto() {
    }

    public NoteResponseDto(Long id, String title, String description, boolean pinned, boolean archived,
                           boolean trashed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pinned = pinned;
        this.archived = archived;
        this.trashed = trashed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
