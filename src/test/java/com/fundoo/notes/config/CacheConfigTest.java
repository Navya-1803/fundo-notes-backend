package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void testRedisCacheConfiguration() {
        RedisCacheConfiguration configuration = new CacheConfig().redisCacheConfiguration(10);
        assertEquals(Duration.ofMinutes(10), configuration.getTtlFunction().getTimeToLive("key", "value"));
        assertFalse(configuration.getAllowCacheNullValues());
        assertEquals("fundoo:notes::", configuration.getKeyPrefixFor("notes"));
    }

    @Test
    void testCachingIsEnabled() {
        assertTrue(CacheConfig.class.isAnnotationPresent(EnableCaching.class));
    }
}
