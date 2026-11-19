package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.service.NoteService;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.TokenService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Schedules reminders on notes. The reminder is published to RabbitMQ and
 * processed asynchronously, so the endpoint answers 202 Accepted.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private static final Logger log = LoggerFactory.getLogger(ReminderController.class);

    private final NoteService noteService;
    private final TokenService tokenService;
    private final ReminderEventPublisher reminderEventPublisher;

    public ReminderController(NoteService noteService, TokenService tokenService, ReminderEventPublisher reminderEventPublisher) {
        this.noteService = noteService;
        this.tokenService = tokenService;
        this.reminderEventPublisher = reminderEventPublisher;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReminder(@Valid @RequestBody ReminderRequestDto requestDto,
                                                              @RequestHeader("Authorization") String token) {
        log.debug("POST /api/reminders for note id: {}", requestDto.getNoteId());
        // fails with 404 if the note does not belong to the caller
        NoteResponseDto note = noteService.getNoteById(requestDto.getNoteId(), token);
        Long userId = tokenService.extractUserId(token);
        reminderEventPublisher.publish(new ReminderEvent(note.getId(), userId, note.getTitle(),
                requestDto.getReminderTime(), requestDto.getMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Reminder scheduled successfully");
        body.put("noteId", note.getId());
        body.put("reminderTime", requestDto.getReminderTime());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
