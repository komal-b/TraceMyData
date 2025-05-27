package com.tracemydata.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;
import com.tracemydata.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow requests from frontend (adjust as needed)
public class AuthController {

    private final AuthService authService;
    private Logger loggers = LoggerFactory.getLogger(AuthController.class);

    // Inject AuthService through constructor
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register a new user with email/password
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        loggers.info("Registering user with email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    // Local login with email and password
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Login with Google ID token (sent from frontend)
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        return ResponseEntity.ok(authService.loginWithGoogle(idToken));
    }

    // Login with Outlook access token
    @PostMapping("/outlook")
    public ResponseEntity<AuthResponse> loginWithOutlook(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        return ResponseEntity.ok(authService.loginWithOutlook(accessToken));
    }
}
