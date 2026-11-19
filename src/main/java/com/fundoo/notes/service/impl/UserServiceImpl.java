package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.UserService;

import org.springframework.stereotype.Service;

/**
 * Registers users and checks their credentials at login.
 *
 * @author Navya Raj
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        // emails are stored in lower case so the same address cannot register twice
        String email = requestDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User(requestDto.getFirstName().trim(), requestDto.getLastName(), email, requestDto.getPassword());
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!requestDto.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return new LoginResponseDto(null, "Login successful");
    }

    // converts the entity into the response sent to the client
    private UserResponseDto mapToUserResponse(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getCreatedAt());
    }
}
