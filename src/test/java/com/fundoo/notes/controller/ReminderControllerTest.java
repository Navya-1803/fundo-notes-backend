package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.service.NoteService;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
class ReminderControllerTest {

    private static final String TOKEN = "Bearer token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private ReminderEventPublisher reminderEventPublisher;

    private String body(LocalDateTime time) {
        return "{\"noteId\": 1, \"reminderTime\": \"" + time.withNano(0) + "\", \"message\": \"Call mom\"}";
    }

    @Test
    void testCreateReminder_Returns202AndPublishes() throws Exception {
        when(noteService.getNoteById(1L, TOKEN)).thenReturn(new NoteResponseDto(1L, "Call", "", false, false, false, null, null));
        when(tokenService.extractUserId(TOKEN)).thenReturn(7L);

        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().plusDays(1))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Reminder scheduled successfully"))
                .andExpect(jsonPath("$.noteId").value(1));

        ArgumentCaptor<ReminderEvent> captor = ArgumentCaptor.forClass(ReminderEvent.class);
        verify(reminderEventPublisher).publish(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals("Call mom", captor.getValue().getMessage());
    }

    @Test
    void testPastReminderTime_Returns400() throws Exception {
        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.reminderTime").value("Reminder time must be in the future"));
        verifyNoInteractions(reminderEventPublisher);
    }

    @Test
    void testNoteOfOtherUser_Returns404() throws Exception {
        when(noteService.getNoteById(1L, TOKEN)).thenThrow(new NoteNotFoundException(1L));
        mockMvc.perform(post("/api/reminders").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDateTime.now().plusDays(1))))
                .andExpect(status().isNotFound());
        verify(reminderEventPublisher, never()).publish(any());
    }
}
