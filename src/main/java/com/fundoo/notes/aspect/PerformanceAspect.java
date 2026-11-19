package com.fundoo.notes.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Measures how long service and controller methods take. Slow methods are
 * logged as warnings so they are easy to find.
 *
 * @author Navya Raj
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    private final long slowThresholdMs;

    public PerformanceAspect(@Value("${app.aop.slow-method-threshold-ms:500}") long slowThresholdMs) {
        this.slowThresholdMs = slowThresholdMs;
    }

    @Around("com.fundoo.notes.aspect.LoggingAspect.serviceMethods() || com.fundoo.notes.aspect.LoggingAspect.controllerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            // measured in finally so failing methods are timed as well
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String method = joinPoint.getSignature().toShortString();
            if (elapsedMs > slowThresholdMs) {
                log.warn("{} took {} ms (slow, threshold {} ms)", method, elapsedMs, slowThresholdMs);
            } else {
                log.debug("{} took {} ms", method, elapsedMs);
            }
        }
    }
}
