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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordService passwordService;
    private TokenService tokenService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);
        tokenService = mock(TokenService.class);
        userService = new UserServiceImpl(userRepository, passwordService, tokenService);
        when(passwordService.encode(any())).thenAnswer(invocation -> "hashed-" + invocation.getArgument(0));
        when(passwordService.matches(any(), any())).thenAnswer(invocation ->
                ("hashed-" + invocation.getArgument(0)).equals(invocation.getArgument(1)));
    }

    private User storedUser() {
        User user = new User("Navya", "Raj", "navya@example.com", "hashed-secret123");
        user.setId(1L);
        return user;
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponseDto response = userService.register(new UserRegisterRequestDto("Navya", "Raj", "Navya@Example.com", "secret123"));

        assertEquals(1L, response.getId());
        assertEquals("navya@example.com", response.getEmail());
    }

    @Test
    void testRegister_StoresEmailInLowerCase() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.register(new UserRegisterRequestDto("Navya", "Raj", " NAVYA@EXAMPLE.COM ", "secret123"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("navya@example.com", captor.getValue().getEmail());
    }

    @Test
    void testRegister_StoresEncodedPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-secret123", captor.getValue().getPassword());
    }

    @Test
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(true);
        assertThrows(DuplicateEmailException.class,
                () -> userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-token");
        LoginResponseDto response = userService.login(new LoginRequestDto("navya@example.com", "secret123"));
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void testLogin_UnknownEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> userService.login(new LoginRequestDto("x@example.com", "secret123")));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        assertThrows(InvalidCredentialsException.class, () -> userService.login(new LoginRequestDto("navya@example.com", "wrong123")));
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void testRegister_WritesLogs(CapturedOutput output) {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123"));
        assertTrue(output.getOut().contains("Registration request received for email: navya@example.com"));
        assertTrue(output.getOut().contains("User registered successfully with id: 11"));
        // the raw password must never be logged
        assertFalse(output.getOut().contains("secret123"));
    }
}
