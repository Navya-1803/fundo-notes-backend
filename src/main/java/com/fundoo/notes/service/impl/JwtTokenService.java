package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT based implementation of TokenService. The user id is stored as the
 * token subject and the email as an extra claim. Tokens are signed with
 * HMAC-SHA using the secret from application.properties.
 *
 * @author Navya Raj
 */
@Service
public class JwtTokenService implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EMAIL_CLAIM = "email";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // HMAC-SHA256 needs a key of at least 256 bits (32 bytes)
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        log.debug("Generating token for user id: {}", user.getId());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(EMAIL_CLAIM, user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).get(EMAIL_CLAIM, String.class);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (InvalidCredentialsException e) {
            return false;
        }
    }

    // verifies the signature and expiry, accepts tokens with or without the "Bearer " prefix
    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException("Authorization token is missing");
        }
        String rawToken = token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
        try {
            return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(rawToken.trim()).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid or expired token", e);
        }
    }
}
