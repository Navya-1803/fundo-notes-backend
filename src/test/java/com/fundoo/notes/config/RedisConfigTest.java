package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    void testRedisTemplate_UsesStringKeysAndJsonValues() {
        RedisTemplate<String, Object> template = new RedisConfig().redisTemplate(mock(RedisConnectionFactory.class));
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertNotNull(template.getValueSerializer());
        assertFalse(template.getValueSerializer() instanceof StringRedisSerializer);
    }
}
