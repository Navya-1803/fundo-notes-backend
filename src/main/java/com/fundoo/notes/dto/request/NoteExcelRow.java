package com.fundoo.notes.dto.request;

/**
 * One row of the notes Excel sheet. Columns in order:
 * Title | Description | Owner Email | Pinned | Archived
 *
 * @author Navya Raj
 */
public class NoteExcelRow {

    // row number in the sheet, used in log messages for invalid rows
    private int rowNumber;
    private String title;
    private String description;
    private String ownerEmail;
    private boolean pinned;
    private boolean archived;

    public NoteExcelRow() {
    }

    public NoteExcelRow(int rowNumber, String title, String description, String ownerEmail, boolean pinned, boolean archived) {
        this.rowNumber = rowNumber;
        this.title = title;
        this.description = description;
        this.ownerEmail = ownerEmail;
        this.pinned = pinned;
        this.archived = archived;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    @Override
    public String toString() {
        return "NoteExcelRow{row=" + rowNumber + ", title='" + title + "', ownerEmail='" + ownerEmail + "'}";
    }
}
