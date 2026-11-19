package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.NoteService;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Checks the caching annotations of NoteServiceImpl with an in-memory cache.
 */
@SpringJUnitConfig(NoteServiceImplCacheTest.TestConfig.class)
class NoteServiceImplCacheTest {

    private static final String TOKEN = "Bearer token";

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(NoteServiceImpl.NOTES_CACHE);
        }

        @Bean
        NoteRepository noteRepository() {
            return mock(NoteRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        // same bean name as the real token service, the cache key expression refers to it
        @Bean
        TokenService jwtTokenService() {
            return mock(TokenService.class);
        }

        @Bean
        NoteService noteService(NoteRepository noteRepository, UserRepository userRepository, TokenService jwtTokenService) {
            return new NoteServiceImpl(noteRepository, userRepository, jwtTokenService);
        }
    }

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService jwtTokenService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(noteRepository, userRepository, jwtTokenService);
        cacheManager.getCache(NoteServiceImpl.NOTES_CACHE).clear();
        User user = new User("Navya", "Raj", "navya@example.com", "hash");
        user.setId(1L);
        when(jwtTokenService.extractUserId(TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteRepository.findByUserIdAndArchivedFalseAndTrashedFalse(1L)).thenReturn(List.of(new Note("Cached", "", user)));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testGetAllNotes_SecondCallServedFromCache() {
        assertEquals(1, noteService.getAllNotes(TOKEN).size());
        assertEquals(1, noteService.getAllNotes(TOKEN).size());
        verify(noteRepository, times(1)).findByUserIdAndArchivedFalseAndTrashedFalse(1L);
    }

    @Test
    void testCreateNote_EvictsCache() {
        noteService.getAllNotes(TOKEN);
        noteService.createNote(new NoteRequestDto("New", ""), TOKEN);
        noteService.getAllNotes(TOKEN);
        verify(noteRepository, times(2)).findByUserIdAndArchivedFalseAndTrashedFalse(1L);
    }

    @Test
    void testCacheKey_IsUserId() {
        noteService.getAllNotes(TOKEN);
        assertNotNull(cacheManager.getCache(NoteServiceImpl.NOTES_CACHE).get(1L));
    }
}
