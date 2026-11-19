package com.fundoo.notes.aspect;

import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.impl.PasswordServiceImpl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class LoggingAspectTest {

    private PasswordService proxy;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        // DEBUG is needed to see the entry and exit messages
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        AspectJProxyFactory factory = new AspectJProxyFactory(new PasswordServiceImpl(new BCryptPasswordEncoder()));
        // class based proxy so the logged signature shows the implementation class
        factory.setProxyTargetClass(true);
        factory.addAspect(new LoggingAspect());
        proxy = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(LoggingAspect.class)).setLevel(previousLevel);
    }

    @Test
    void testLogsEntryAndExit_WithoutArguments(CapturedOutput output) {
        proxy.encode("secret123");
        assertTrue(output.getOut().contains("Entering PasswordServiceImpl.encode(..)"));
        assertTrue(output.getOut().contains("Exiting PasswordServiceImpl.encode(..)"));
        assertFalse(output.getOut().contains("secret123"));
    }

    @Test
    void testLogsExceptions(CapturedOutput output) {
        assertThrows(IllegalArgumentException.class, () -> proxy.encode(" "));
        assertTrue(output.getOut().contains("Exception in PasswordServiceImpl.encode(..): IllegalArgumentException"));
    }
}
