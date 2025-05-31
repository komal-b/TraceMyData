package com.tracemydata.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;
import com.tracemydata.model.User;
import com.tracemydata.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<String> initiateRegistration(@RequestBody RegisterRequest request) {
        try{
            loggers.info("Initiating registration for: {}", request.getEmail());
            String message = authService.initiateRegistration(request);
            return ResponseEntity.ok(message); // returns: "Verification email sent
        }catch (Exception e) {
            loggers.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        loggers.info("Verifying token: {}", token);
        try {
            return authService.register(token);
            // "Email verified. Account created."
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Local login with email and password
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Login with Google ID token (sent from frontend)
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody Map<String, String> body) {
        loggers.error(body.get(body.get("idToken")));
        String idToken = body.get("idToken");
        return ResponseEntity.ok(authService.loginWithGoogle(idToken));
    }

     @PostMapping("/complete-registration")
    public ResponseEntity<?> completeProfile(@RequestBody User userDto, Principal principal) {
        String email = principal.getName(); // fetched from JWT token
        authService.completeUserProfile(email, userDto);
        return ResponseEntity.ok("Profile completed");
    }

    
}
