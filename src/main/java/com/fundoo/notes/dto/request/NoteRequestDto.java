package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used to create a note.
 *
 * @author Navya Raj
 */
public class NoteRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    public NoteRequestDto() {
    }

    public NoteRequestDto(String title, String description) {
        this.title = title;
        this.description = description;
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
}
