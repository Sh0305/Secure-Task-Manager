package com.securetask.taskmanager.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securetask.taskmanager.dto.AuthResponse;
import com.securetask.taskmanager.dto.LoginRequest;
import com.securetask.taskmanager.dto.RegisterRequest;
import com.securetask.taskmanager.exception.DuplicateResourceException;
import com.securetask.taskmanager.exception.ResourceNotFoundException;
import com.securetask.taskmanager.model.RefreshToken;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {
    System.out.println("Step 1 - authenticating");
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));

    System.out.println("Step 2 - loading user details");
    UserDetails userDetails = userDetailsService
            .loadUserByUsername(request.getEmail());

    System.out.println("Step 3 - finding user in db");
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    System.out.println("Step 4 - generating access token");
    String accessToken = jwtService.generateToken(userDetails);

    System.out.println("Step 5 - creating refresh token");
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    System.out.println("Step 6 - building response");
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
        System.out.println("Step 7 - response built: " + response.toString());
        return response;
        
}

    public String logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
        refreshTokenService.deleteUserTokens(user);
        return "Logged out successfully";
    }
    
}