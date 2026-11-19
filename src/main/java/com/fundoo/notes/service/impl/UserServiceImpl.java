package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.service.UserService;

import org.springframework.stereotype.Service;

/**
 * Registers users with a hashed password and issues a JWT on login.
 *
 * @author Navya Raj
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public UserServiceImpl(UserRepository userRepository, PasswordService passwordService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @Override
    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        // emails are stored in lower case so the same address cannot register twice
        String email = requestDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered: " + email);
        }
        // only the BCrypt hash of the password is stored
        String encodedPassword = passwordService.encode(requestDto.getPassword());
        User user = new User(requestDto.getFirstName().trim(), requestDto.getLastName(), email, encodedPassword);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordService.matches(requestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = tokenService.generateToken(user);
        return new LoginResponseDto(token, "Login successful");
    }

    // converts the entity into the response sent to the client
    private UserResponseDto mapToUserResponse(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getCreatedAt());
    }
}
