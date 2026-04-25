package com.securetask.taskmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securetask.taskmanager.dto.LoginRequest;
import com.securetask.taskmanager.dto.RegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturn201_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("newuser@test.com");
        request.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void register_ShouldReturn400_WhenPasswordTooWeak() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("newuser@test.com");
        request.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturn400_WhenEmailInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("notanemail");
        request.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void register_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("duplicate@test.com");
        request.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldReturn200WithToken_WhenCredentialsCorrect() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Login Test User");
        registerRequest.setEmail("logintest@test.com");
        registerRequest.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("logintest@test.com");
        loginRequest.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("logintest@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_ShouldReturn401_WhenWrongPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Wrong Pass User");
        registerRequest.setEmail("wrongpass@test.com");
        registerRequest.setPassword("Test1234!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@test.com");
        loginRequest.setPassword("WrongPassword1!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}