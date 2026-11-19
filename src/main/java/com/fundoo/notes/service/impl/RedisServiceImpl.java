package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.RedisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * RedisTemplate based implementation of RedisService.
 *
 * @author Navya Raj
 */
@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration defaultTtl;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate,
                            @Value("${app.redis.token-expiration}") long defaultTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.defaultTtl = Duration.ofSeconds(defaultTtlSeconds);
    }

    @Override
    public void save(String key, Object value, Duration ttl) {
        validateKey(key);
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Time to live must be positive");
        }
        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("Saved key: {} with ttl: {}s", key, ttl.toSeconds());
    }

    @Override
    public void save(String key, Object value) {
        save(key, value, defaultTtl);
    }

    @Override
    public Optional<Object> get(String key) {
        validateKey(key);
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Lookup key: {} -> {}", key, value == null ? "miss" : "hit");
        return Optional.ofNullable(value);
    }

    @Override
    public boolean delete(String key) {
        validateKey(key);
        boolean deleted = Boolean.TRUE.equals(redisTemplate.delete(key));
        log.debug("Delete key: {} -> {}", key, deleted);
        return deleted;
    }

    @Override
    public boolean exists(String key) {
        validateKey(key);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
    }
}
