package com.fundoo.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceImplTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private RedisServiceImpl redisService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisService = new RedisServiceImpl(redisTemplate, 3600);
    }

    @Test
    void testSave_WithTtl() {
        redisService.save("key", "value", Duration.ofSeconds(60));
        verify(valueOperations).set("key", "value", Duration.ofSeconds(60));
    }

    @Test
    void testSave_UsesDefaultTtl() {
        redisService.save("key", "value");
        verify(valueOperations).set("key", "value", Duration.ofSeconds(3600));
    }

    @Test
    void testSave_InvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> redisService.save(" ", "v"));
        assertThrows(IllegalArgumentException.class, () -> redisService.save("k", null));
        assertThrows(IllegalArgumentException.class, () -> redisService.save("k", "v", Duration.ZERO));
    }

    @Test
    void testGet_HitAndMiss() {
        when(valueOperations.get("present")).thenReturn("value");
        assertEquals("value", redisService.get("present").orElseThrow());
        assertTrue(redisService.get("absent").isEmpty());
    }

    @Test
    void testDeleteAndExists() {
        when(redisTemplate.delete("key")).thenReturn(true);
        when(redisTemplate.hasKey("key")).thenReturn(true);
        assertTrue(redisService.delete("key"));
        assertTrue(redisService.exists("key"));
        assertFalse(redisService.exists("other"));
    }
}
