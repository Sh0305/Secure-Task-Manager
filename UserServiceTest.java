package com.securetask.taskmanager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.securetask.taskmanager.dto.AuthResponse;
import com.securetask.taskmanager.dto.LoginRequest;
import com.securetask.taskmanager.dto.RegisterRequest;
import com.securetask.taskmanager.exception.DuplicateResourceException;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.UserRepository;
import com.securetask.taskmanager.service.CustomUserDetailsService;
import com.securetask.taskmanager.service.JwtService;
import com.securetask.taskmanager.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;


    @Test
    void register_ShouldSucceed_WhenEmailNotTaken() {
        when(userRepository.existsByEmail("priya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Priya123!")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        String result = userService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Priya123!");
    }

    @Test
    void register_ShouldThrowDuplicateException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("priya@test.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.register(registerRequest)
        );

        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldHashPassword_NeverStorePlainText() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Priya123!")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotEquals("Priya123!", savedUser.getPassword());
            assertEquals("$2a$10$hashedpassword", savedUser.getPassword());
            return savedUser;
        });

        userService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("Priya123!");
    }

    @Test
    void register_ShouldAssignUserRole_ByDefault() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(User.Role.USER, savedUser.getRole());
            return savedUser;
        });

        userService.register(registerRequest);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "priya@test.com",
                "$2a$10$hashedpassword",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("priya@test.com"))
                .thenReturn(userDetails);
        when(userRepository.findByEmail("priya@test.com"))
                .thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(userDetails))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.test.token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertEquals("priya@test.com", response.getEmail());
        assertEquals("Priya Sharma", response.getName());
        assertEquals("USER", response.getRole());
    }

    @Test
    void login_ShouldCallAuthenticationManager() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "priya@test.com",
                "hashed",
                List.of()
        );

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any())).thenReturn("token");

        userService.login(loginRequest);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));
    }
}
