package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET = "FundooNotesTestSecretKeyThatIsLongEnough123456";

    private final JwtTokenService tokenService = new JwtTokenService(SECRET, 60_000);

    private User user() {
        User user = new User("Navya", "Raj", "navya@example.com", "hash");
        user.setId(42L);
        return user;
    }

    @Test
    void testGenerateToken_ContainsUserDetails() {
        String token = tokenService.generateToken(user());
        assertEquals(42L, tokenService.extractUserId(token));
        assertEquals("navya@example.com", tokenService.extractEmail(token));
        assertTrue(tokenService.isTokenValid(token));
    }

    @Test
    void testBearerPrefix_IsAccepted() {
        String token = tokenService.generateToken(user());
        assertEquals(42L, tokenService.extractUserId("Bearer " + token));
    }

    @Test
    void testTamperedToken_IsRejected() {
        String token = tokenService.generateToken(user());
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertFalse(tokenService.isTokenValid(tampered));
        assertThrows(IllegalArgumentException.class, () -> tokenService.extractUserId(tampered));
    }

    @Test
    void testTokenFromOtherSecret_IsRejected() {
        JwtTokenService other = new JwtTokenService("AnotherSecretKeyThatIsAlsoLongEnough987654", 60_000);
        assertFalse(tokenService.isTokenValid(other.generateToken(user())));
    }

    @Test
    void testExpiredToken_IsRejected() {
        JwtTokenService shortLived = new JwtTokenService(SECRET, -1000);
        assertFalse(shortLived.isTokenValid(shortLived.generateToken(user())));
    }

    @Test
    void testMissingToken_IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> tokenService.extractUserId(null));
        assertThrows(IllegalArgumentException.class, () -> tokenService.extractUserId(" "));
    }

    @Test
    void testShortSecret_IsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenService("short", 60_000));
    }
}
