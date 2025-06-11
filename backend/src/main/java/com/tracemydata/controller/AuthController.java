package com.tracemydata.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;

import com.tracemydata.service.AuthService;


import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity verifyUser(@RequestParam("token") String token) {
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

    @PostMapping("/update-profile")
public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData, @RequestHeader("Authorization") String token) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    try {
        Map<String, Object> response = new HashMap<>();
        if(profileData.get("newEmail") == null || profileData.get("newEmail").isEmpty()) {
            // Suppose this returns updated user info object or string message
            AuthResponse result = authService.updateProfile(profileData, username);
            response.put("message", "Profile updated successfully!");
            response.put("user", result);
        } else {
            String message = authService.updateEmail(profileData, username);
            response.put("message", message);
        }
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        loggers.error("Profile update error: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok("Password reset link sent to your email");
        } catch (Exception e) {
            loggers.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and new password are required");
        }
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            loggers.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {
    
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Old and new passwords are required");
        }
        try {
            authService.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            loggers.error("Change password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
}
