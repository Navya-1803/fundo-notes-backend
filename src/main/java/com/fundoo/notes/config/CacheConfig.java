package com.fundoo.notes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Enables Spring caching. With spring.cache.type=redis Spring Boot builds a
 * Redis cache manager that uses the configuration defined here.
 *
 * @author Navya Raj
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(@Value("${app.cache.notes-ttl-minutes:10}") long ttlMinutes) {
        return RedisCacheConfiguration.defaultCacheConfig()
                // cached entries expire so the cache cannot grow forever
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .prefixCacheNameWith("fundoo:")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()));
    }
}
