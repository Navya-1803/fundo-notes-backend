package com.fundoo.notes.exception;

/**
 * Thrown when login details are wrong or a token is missing, invalid or expired.
 *
 * @author Navya Raj
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
