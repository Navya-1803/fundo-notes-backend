package com.fundoo.notes.aspect;

import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.impl.PasswordServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class PerformanceAspectTest {

    private PasswordService proxy(long thresholdMs) {
        AspectJProxyFactory factory = new AspectJProxyFactory(new PasswordServiceImpl(new BCryptPasswordEncoder()));
        // class based proxy so the logged signature shows the implementation class
        factory.setProxyTargetClass(true);
        factory.addAspect(new PerformanceAspect(thresholdMs));
        return factory.getProxy();
    }

    @Test
    void testSlowMethod_LoggedAsWarning(CapturedOutput output) {
        // a threshold of -1 ms makes every call count as slow
        proxy(-1).encode("secret123");
        assertTrue(output.getOut().contains("PasswordServiceImpl.encode(..) took"));
        assertTrue(output.getOut().contains("(slow, threshold -1 ms)"));
    }

    @Test
    void testResultIsReturnedUnchanged() {
        PasswordService service = proxy(10_000);
        String encoded = service.encode("secret123");
        assertTrue(service.matches("secret123", encoded));
    }

    @Test
    void testFailingMethod_IsStillTimedAndRethrown(CapturedOutput output) {
        assertThrows(IllegalArgumentException.class, () -> proxy(-1).encode(""));
        assertTrue(output.getOut().contains("PasswordServiceImpl.encode(..) took"));
    }
}
