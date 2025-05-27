package com.tracemydata.service;

import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;
import com.tracemydata.model.User;
import com.tracemydata.repository.UserRepository;
import com.tracemydata.util.JwtUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    // Constructor initializes dependencies
    public AuthService(UserRepository userRepo, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    // Handles user registration for local auth
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email is already registered
        userRepo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });

        // Create new user entity
        User newUser = new User();

        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setAuthProvider("local");

        // Save to database and generate token
        User savedUser = userRepo.save(newUser);
        String token = jwtUtil.generateToken(savedUser);

        // Return response with token and user info
        return mapToAuthResponse(savedUser, token);
    }

    // Handles login for local users
    public AuthResponse login(LoginRequest request) {
        // Look up user by email
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Reject if the account is an OAuth account
        if (!user.getAuthProvider().equals("local")) {
            throw new RuntimeException("This account uses " + user.getAuthProvider() + " login");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate token
        String token = jwtUtil.generateToken(user);
        return mapToAuthResponse(user, token);
    }

    // Handles login via Google OAuth
    public AuthResponse loginWithGoogle(String idToken) {
        Map<String, Object> payload = jwtUtil.verifyGoogleToken(idToken);
        return handleOAuthLogin(payload, "google");
    }

    // Handles login via Outlook OAuth
    public AuthResponse loginWithOutlook(String accessToken) {
        Map<String, Object> payload = jwtUtil.verifyOutlookToken(accessToken);
        return handleOAuthLogin(payload, "outlook");
    }

    // Shared method for processing OAuth logins (Google/Outlook)
    @Transactional
    private AuthResponse handleOAuthLogin(Map<String, Object> payload, String provider) {
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");

        // If user doesn't exist, register a new OAuth user
        User user = userRepo.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID());
            newUser.setEmail(email);
            newUser.setAuthProvider(provider);
            return userRepo.save(newUser);
        });

        // Generate token and return response
        String token = jwtUtil.generateToken(user);
        return mapToAuthResponse(user, token);
    }

    // Utility to map user entity + token into AuthResponse DTO
    private AuthResponse mapToAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setEmail(user.getEmail());
        response.setAuthProvider(user.getAuthProvider());
        response.setToken(token);
        return response;
    }
}
