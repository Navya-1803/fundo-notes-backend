package com.fundoo.notes.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObjectMapper used to turn event objects into JSON messages and back.
 *
 * @author Navya Raj
 */
@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper eventObjectMapper() {
        // unknown fields are ignored so older consumers keep working when events grow
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
