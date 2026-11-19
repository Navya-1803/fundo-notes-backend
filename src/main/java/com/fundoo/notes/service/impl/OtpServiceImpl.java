package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.RedisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * Generates 6 digit OTPs and keeps them in Redis until they expire. An OTP
 * can be used only once, it is deleted after a successful verification.
 *
 * @author Navya Raj
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private static final String OTP_KEY_PREFIX = "otp:";

    private final SecureRandom random = new SecureRandom();
    private final RedisService redisService;
    private final Duration otpTtl;

    public OtpServiceImpl(RedisService redisService, @Value("${app.redis.otp-expiration}") long otpExpirationSeconds) {
        this.redisService = redisService;
        this.otpTtl = Duration.ofSeconds(otpExpirationSeconds);
    }

    @Override
    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        // a new OTP replaces any older one for the same email
        redisService.save(key(email), otp, otpTtl);
        log.info("OTP generated for {}, valid for {} seconds", email, otpTtl.toSeconds());
        return otp;
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        boolean valid = redisService.get(key(email)).map(stored -> stored.toString().equals(otp)).orElse(false);
        if (valid) {
            redisService.delete(key(email));
            log.info("OTP verified for {}", email);
        } else {
            log.warn("OTP verification failed for {}", email);
        }
        return valid;
    }

    private String key(String email) {
        return OTP_KEY_PREFIX + email.trim().toLowerCase();
    }
}
