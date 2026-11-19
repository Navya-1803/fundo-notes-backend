package com.fundoo.notes.controller;

import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.RedisService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedisController.class)
class RedisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    private OtpService otpService;

    @Test
    void testSave_DefaultTtl() throws Exception {
        mockMvc.perform(post("/api/redis/save").param("key", "greeting").param("value", "hello"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("greeting"));
        verify(redisService).save("greeting", "hello");
    }

    @Test
    void testSave_CustomTtl() throws Exception {
        mockMvc.perform(post("/api/redis/save").param("key", "k").param("value", "v").param("ttlSeconds", "60"))
                .andExpect(status().isCreated());
        verify(redisService).save("k", "v", Duration.ofSeconds(60));
    }

    @Test
    void testSave_MissingValue_Returns400() throws Exception {
        mockMvc.perform(post("/api/redis/save").param("key", "k"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing request parameter: value"));
    }

    @Test
    void testGet_FoundAndNotFound() throws Exception {
        when(redisService.get("greeting")).thenReturn(Optional.of("hello"));
        when(redisService.get("missing")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/redis/get/greeting")).andExpect(status().isOk()).andExpect(jsonPath("$.value").value("hello"));
        mockMvc.perform(get("/api/redis/get/missing")).andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        when(redisService.delete("greeting")).thenReturn(true);
        mockMvc.perform(delete("/api/redis/delete/greeting")).andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void testOtpGenerateAndVerify() throws Exception {
        when(otpService.generateOtp("navya@example.com")).thenReturn("123456");
        when(otpService.verifyOtp("navya@example.com", "123456")).thenReturn(true);
        mockMvc.perform(post("/api/redis/otp/generate").param("email", "navya@example.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.otp").value("123456"));
        mockMvc.perform(post("/api/redis/otp/verify").param("email", "navya@example.com").param("otp", "123456"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void testOtpVerify_WrongOtp_Returns400() throws Exception {
        when(otpService.verifyOtp("navya@example.com", "000000")).thenReturn(false);
        mockMvc.perform(post("/api/redis/otp/verify").param("email", "navya@example.com").param("otp", "000000"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testOtpGenerate_InvalidEmail_Returns400() throws Exception {
        mockMvc.perform(post("/api/redis/otp/generate").param("email", "not-an-email"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(otpService);
    }
}
