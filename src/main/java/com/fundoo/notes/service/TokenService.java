package com.fundoo.notes.service;

import com.fundoo.notes.entity.User;

/**
 * Creates and reads the authentication tokens given to users at login.
 *
 * @author Navya Raj
 */
public interface TokenService {

    String generateToken(User user);

    /**
     * @return id of the user the token belongs to
     * @throws com.fundoo.notes.exception.InvalidCredentialsException if the token is missing, invalid or expired
     */
    Long extractUserId(String token);

    String extractEmail(String token);

    boolean isTokenValid(String token);
}
