package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String REGISTER_BODY = """
            {"firstName": "Navya", "lastName": "Raj", "email": "navya@example.com", "password": "secret123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void testRegister_Returns201() throws Exception {
        when(userService.register(any())).thenReturn(new UserResponseDto(1L, "Navya", "Raj", "navya@example.com", null));
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("navya@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testRegister_InvalidBody_Returns400() throws Exception {
        String body = """
                {"firstName": "", "email": "bad", "password": "123"}
                """;
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.email").value("Enter a valid email"));
        verifyNoInteractions(userService);
    }

    @Test
    void testLogin_Returns200() throws Exception {
        when(userService.login(any())).thenReturn(new LoginResponseDto("jwt-token", "Login successful"));
        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"navya@example.com\", \"password\": \"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        when(userService.register(any())).thenThrow(new DuplicateEmailException("Email already registered: navya@example.com"));
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/users/register"));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        when(userService.login(any())).thenThrow(new InvalidCredentialsException("Invalid email or password"));
        mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"navya@example.com\", \"password\": \"wrong123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
