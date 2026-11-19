package com.fundoo.notes.config;

import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * Enables @JmsListener methods and configures the listener containers.
 *
 * @author Navya Raj
 */
@Configuration
@EnableJms
public class JmsConfig {

    private static final Logger log = LoggerFactory.getLogger(JmsConfig.class);

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Value("${spring.jms.listener.auto-startup:true}") boolean autoStartup) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // between one and three consumers depending on the load
        factory.setConcurrency("1-3");
        factory.setAutoStartup(autoStartup);
        // a failing message is logged instead of silently lost
        factory.setErrorHandler(error -> log.error("Error while processing JMS message: {}", error.getMessage()));
        return factory;
    }
}
