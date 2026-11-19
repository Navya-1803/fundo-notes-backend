package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    private User storedUser() {
        User user = new User("Navya", "Raj", "navya@example.com", "secret123");
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
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        LoginResponseDto response = userService.login(new LoginRequestDto("navya@example.com", "secret123"));
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void testLogin_UnknownEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.login(new LoginRequestDto("x@example.com", "secret123")));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        assertThrows(IllegalArgumentException.class, () -> userService.login(new LoginRequestDto("navya@example.com", "wrong123")));
    }
}
