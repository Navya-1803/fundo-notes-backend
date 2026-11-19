package com.fundoo.notes.service;

import java.time.Duration;
import java.util.Optional;

/**
 * Simple key-value operations on Redis. Every value has a time to live,
 * Redis is used as a fast temporary store and never as the main database.
 *
 * @author Navya Raj
 */
public interface RedisService {

    void save(String key, Object value, Duration ttl);

    // saves with the default time to live
    void save(String key, Object value);

    Optional<Object> get(String key);

    boolean delete(String key);

    boolean exists(String key);
}
