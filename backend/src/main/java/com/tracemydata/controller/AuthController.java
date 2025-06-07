package com.tracemydata.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;

import com.tracemydata.service.AuthService;


import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<String> initiateRegistration(@RequestBody RegisterRequest request) {
        try{
            String message = authService.initiateRegistration(request);
            return ResponseEntity.ok(message); // returns: "Verification email sent
        }catch (Exception e) {
            loggers.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        try {
            return authService.register(token);
            // "Email verified. Account created."
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Local login with email and password
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try{
            return ResponseEntity.ok(authService.login(request));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }

    // Login with Google ID token (sent from frontend)
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String token = body.get("idToken");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("ID token is required");
        }
        return ResponseEntity.ok(authService.loginWithGoogle(token));
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout() {
        // Clear the JWT cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .path("/") 
                .sameSite("Strict")// Set path to root
                .maxAge(0) // Set max age to 0 to delete it
                .httpOnly(true)
                .secure(true) // HttpOnly for security
                .build();
        loggers.info("User logged out, clearing JWT cookie");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
    }


    
}
