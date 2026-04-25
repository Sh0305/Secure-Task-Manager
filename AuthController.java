package com.securetask.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securetask.taskmanager.dto.AuthResponse;
import com.securetask.taskmanager.dto.LoginRequest;
import com.securetask.taskmanager.dto.RefreshTokenRequest;
import com.securetask.taskmanager.dto.RegisterRequest;
import com.securetask.taskmanager.model.RefreshToken;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.service.CustomUserDetailsService;
import com.securetask.taskmanager.service.JwtService;
import com.securetask.taskmanager.service.RefreshTokenService;
import com.securetask.taskmanager.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        // Verify token is valid and not expired
        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(request.getRefreshToken());

        // Rotate — old invalidated, new created
        RefreshToken newRefreshToken = refreshTokenService
                .rotateRefreshToken(refreshToken);

        // Generate new access token
        UserDetails userDetails = userDetailsService
                .loadUserByUsername(
                        refreshToken.getUser().getEmail());
        String newAccessToken = jwtService.generateToken(userDetails);

        User user = refreshToken.getUser();

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        String email = org.springframework.security.core.context
                .SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return ResponseEntity.ok(userService.logout(email));
    }
}