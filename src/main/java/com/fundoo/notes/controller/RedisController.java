package com.fundoo.notes.controller;

import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.RedisService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoints to try out Redis: generic key-value operations and OTPs.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/redis")
@Validated
public class RedisController {

    private static final Logger log = LoggerFactory.getLogger(RedisController.class);

    private final RedisService redisService;
    private final OtpService otpService;

    public RedisController(RedisService redisService, OtpService otpService) {
        this.redisService = redisService;
        this.otpService = otpService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestParam @NotBlank String key,
                                                    @RequestParam @NotBlank String value,
                                                    @RequestParam(required = false) @Positive Long ttlSeconds) {
        log.debug("POST /api/redis/save key: {}", key);
        if (ttlSeconds == null) {
            redisService.save(key, value);
        } else {
            redisService.save(key, value, Duration.ofSeconds(ttlSeconds));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response("message", "Value saved", "key", key));
    }

    @GetMapping("/get/{key}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String key) {
        log.debug("GET /api/redis/get/{}", key);
        return redisService.get(key)
                .map(value -> ResponseEntity.ok(response("key", key, "value", value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(response("message", "Key not found or expired", "key", key)));
    }

    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key) {
        log.debug("DELETE /api/redis/delete/{}", key);
        boolean deleted = redisService.delete(key);
        return ResponseEntity.ok(response("key", key, "deleted", deleted));
    }

    @PostMapping("/otp/generate")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestParam @Email @NotBlank String email) {
        log.debug("POST /api/redis/otp/generate for {}", email);
        String otp = otpService.generateOtp(email);
        // no mail service is connected yet, so the OTP is returned to make testing possible
        return ResponseEntity.ok(response("message", "OTP generated", "otp", otp));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam @Email @NotBlank String email,
                                                         @RequestParam @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits") String otp) {
        log.debug("POST /api/redis/otp/verify for {}", email);
        boolean verified = otpService.verifyOtp(email, otp);
        HttpStatus status = verified ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response("verified", verified,
                "message", verified ? "OTP verified" : "Invalid or expired OTP"));
    }

    private Map<String, Object> response(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(k1, v1);
        body.put(k2, v2);
        return body;
    }
}
