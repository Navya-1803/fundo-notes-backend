package com.fundoo.notes.service;

/**
 * Hashes and verifies user passwords.
 *
 * @author Navya Raj
 */
public interface PasswordService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
