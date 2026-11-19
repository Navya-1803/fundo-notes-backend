package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.RedisService;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OtpServiceImplTest {

    private final RedisService redisService = mock(RedisService.class);
    private final OtpServiceImpl otpService = new OtpServiceImpl(redisService, 300);

    @Test
    void testGenerateOtp_SixDigitsStoredWithExpiry() {
        String otp = otpService.generateOtp("Navya@Example.com");
        assertTrue(otp.matches("\\d{6}"));
        verify(redisService).save("otp:navya@example.com", otp, Duration.ofSeconds(300));
    }

    @Test
    void testVerifyOtp_CorrectOtpIsDeleted() {
        when(redisService.get("otp:navya@example.com")).thenReturn(Optional.of("123456"));
        assertTrue(otpService.verifyOtp("navya@example.com", "123456"));
        verify(redisService).delete("otp:navya@example.com");
    }

    @Test
    void testVerifyOtp_WrongOtp() {
        when(redisService.get("otp:navya@example.com")).thenReturn(Optional.of("123456"));
        assertFalse(otpService.verifyOtp("navya@example.com", "654321"));
        verify(redisService, never()).delete(anyString());
    }

    @Test
    void testVerifyOtp_Expired() {
        when(redisService.get(anyString())).thenReturn(Optional.empty());
        assertFalse(otpService.verifyOtp("navya@example.com", "123456"));
    }
}
