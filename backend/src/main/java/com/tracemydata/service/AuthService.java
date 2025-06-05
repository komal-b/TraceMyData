package com.tracemydata.service;


import com.tracemydata.dto.AuthResponse;
import com.tracemydata.dto.LoginRequest;
import com.tracemydata.dto.RegisterRequest;
// import com.tracemydata.dto.TempUserDTO;
import com.tracemydata.model.TempUser;
import com.tracemydata.model.User;
import com.tracemydata.repository.TempUserRepository;
import com.tracemydata.repository.UserRepository;
import com.tracemydata.util.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private Logger loggers = LoggerFactory.getLogger(AuthService.class);


    private final TempUserRepository tempUserRepository; // Repository for temporary user registrations
    private final EmailService emailService; // Assuming you have an EmailService for sending verification emails
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    // Constructor initializes dependencies
    public AuthService(UserRepository userRepo, TempUserRepository tempUserRepository, EmailService emailService, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.tempUserRepository = tempUserRepository;
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    public String initiateRegistration(RegisterRequest request) {
        userRepo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });

        String token = UUID.randomUUID().toString();
        TempUser temp = new TempUser();
        temp.setFirstName(request.getFirstName());
        temp.setLastName(request.getLastName());
        temp.setEmail(request.getEmail());
        temp.setPassword(passwordEncoder.encode(request.getPassword()));
        temp.setToken(token);
        temp.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailService.sendVerificationEmail(request.getEmail(), token);
        tempUserRepository.save(temp);

        return "Verification email sent";
    }


    // Handles user registration for local auth
    @Transactional
    public ResponseEntity register(String token) {
        // Check if email is already registered
    TempUser tempUser = tempUserRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
    if (tempUser.isExpired()) {
        tempUserRepository.delete(tempUser);
        return ResponseEntity.badRequest().body("Verification link expired. Redirecting to Register...");
    }

    // Create new user entity
    User newUser = new User();

    newUser.setFirstName(tempUser.getFirstName());
    newUser.setLastName(tempUser.getLastName());
    newUser.setEmail(tempUser.getEmail());
    newUser.setPasswordHash(tempUser.getPassword());
    newUser.setAuthProvider("local");

    userRepo.save(newUser);
    tempUserRepository.delete(tempUser); // Clean up temp user

    return ResponseEntity.ok("Email verified! Redirecting to login...");
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
    public String loginWithGoogle(String idToken) {
        Map<String, Object> payload = jwtUtil.verifyGoogleToken(idToken);
        return handleOAuthLogin(payload, "google");
    }


    // Shared method for processing OAuth logins (Google/Outlook)
    @Transactional
    private String handleOAuthLogin(Map<String, Object> payload, String provider) {
        
        try{
            User user = null;
            String email = (String) payload.get("email");
            // If user doesn't exist, register a new OAuth user
            Optional<User> user_email = userRepo.findByEmail(email);
            if(user_email.isEmpty()){
                user = registerNewOAuthUser(payload);
            }
            else{
                user = user_email.get();
            }
            // Generate token and return response
            
            return jwtUtil.generateToken(user);
        }catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database error during OAuth login", e);
        } catch (Exception e) {
             throw new RuntimeException("OAuth login failed", e);
        }   
        
    }
    private User registerNewOAuthUser(Map<String, Object> payload) {
        User newUser = new User();
        newUser.setEmail((String) payload.get("email"));
        newUser.setFirstName((String) payload.get("given_name"));
        newUser.setLastName((String) payload.get("family_name"));
        newUser.setAuthProvider("google");
        return userRepo.save(newUser);
    }

    // Utility to map user entity + token into AuthResponse DTO
    private AuthResponse mapToAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setEmail(user.getEmail());
        response.setAuthProvider(user.getAuthProvider());
        response.setToken(token);
        return response;
    }

    public void completeUserProfile(String email, String firstName, String lastName) {
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        userRepo.save(user);
    }
}
