package com.fundoo.notes.exception;

/**
 * Thrown when someone tries to register with an email that already exists.
 *
 * @author Navya Raj
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
