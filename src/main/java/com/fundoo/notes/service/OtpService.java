package com.fundoo.notes.service;

/**
 * One time passwords used for actions like password reset.
 *
 * @author Navya Raj
 */
public interface OtpService {

    String generateOtp(String email);

    boolean verifyOtp(String email, String otp);
}
