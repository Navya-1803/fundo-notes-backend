package com.fundoo.notes.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message published after a user registers, consumed in the background
 * to send the welcome / verification email.
 *
 * @author Navya Raj
 */
public class UserRegistrationEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private String firstName;
    // ISO-8601 text so the event stays plain JSON
    private String registeredAt;

    public UserRegistrationEvent() {
    }

    public UserRegistrationEvent(Long userId, String email, String firstName) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.registeredAt = LocalDateTime.now().toString();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "UserRegistrationEvent{userId=" + userId + ", email='" + email + "', registeredAt='" + registeredAt + "'}";
    }
}
