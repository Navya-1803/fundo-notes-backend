package com.fundoo.notes.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs entry, exit and failures of every service and controller method in
 * one place instead of repeating the same log lines in each class.
 * Method arguments are not logged because they can contain passwords.
 *
 * @author Navya Raj
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.fundoo.notes.service.impl..*(..))")
    public void serviceMethods() {
        // pointcut signature only
    }

    @Pointcut("execution(* com.fundoo.notes.controller..*(..))")
    public void controllerMethods() {
        // pointcut signature only
    }

    @Before("serviceMethods() || controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.debug("Entering {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning("serviceMethods() || controllerMethods()")
    public void logAfterReturning(JoinPoint joinPoint) {
        log.debug("Exiting {}", joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = "serviceMethods() || controllerMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.warn("Exception in {}: {} - {}", joinPoint.getSignature().toShortString(),
                exception.getClass().getSimpleName(), exception.getMessage());
    }
}
