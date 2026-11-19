package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.UserRegistrationEvent;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.service.UserRegistrationEventPublisher;
import com.fundoo.notes.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registers users with a hashed password and issues a JWT on login.
 *
 * @author Navya Raj
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final UserRegistrationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, PasswordService passwordService, TokenService tokenService,
                           UserRegistrationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        // emails are stored in lower case so the same address cannot register twice
        String email = requestDto.getEmail().trim().toLowerCase();
        log.info("Registration request received for email: {}", email);
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected, email already registered: {}", email);
            throw new DuplicateEmailException("Email already registered: " + email);
        }
        // only the BCrypt hash of the password is stored
        String encodedPassword = passwordService.encode(requestDto.getPassword());
        User user = new User(requestDto.getFirstName().trim(), requestDto.getLastName(), email, encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());
        // the welcome email is sent asynchronously through JMS
        eventPublisher.publish(new UserRegistrationEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName()));
        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String email = requestDto.getEmail().trim().toLowerCase();
        log.info("Login attempt for email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Login failed, no account for email: {}", email);
            return new InvalidCredentialsException("Invalid email or password");
        });
        if (!passwordService.matches(requestDto.getPassword(), user.getPassword())) {
            log.warn("Login failed, wrong password for user id: {}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = tokenService.generateToken(user);
        log.info("Login successful for user id: {}", user.getId());
        return new LoginResponseDto(token, "Login successful");
    }

    // converts the entity into the response sent to the client
    private UserResponseDto mapToUserResponse(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getCreatedAt());
    }
}
