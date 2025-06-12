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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class AuthService {

    private Logger loggers = LoggerFactory.getLogger(AuthService.class);


    private final TempUserRepository tempUserRepository; // Repository for temporary user registrations
    private final EmailService emailService; // Assuming you have an EmailService for sending verification emails
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private TempUserCleanupJob tempUserCleanupJob;

    // Constructor initializes dependencies
    public AuthService(UserRepository userRepo, TempUserCleanupJob tempUserCleanupJob , TempUserRepository tempUserRepository, EmailService emailService, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.tempUserRepository = tempUserRepository;
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.tempUserCleanupJob = tempUserCleanupJob;
    }

    public String initiateRegistration(RegisterRequest request) {
        userRepo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });
        if(tempUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("A registration request is already pending for this email");
        }
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
    public ResponseEntity<?> register(String token) {
    User newUser = null;
        // Check if email is already registered
    TempUser tempUser = tempUserRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or Expired token. Try registering again."));
    
    if (tempUser.isExpired()) {
        tempUserRepository.delete(tempUser);
        
    }


    // Create new user entity
    if(tempUser.getUser_id() != null) {
        newUser = userRepo.findById(tempUser.getUser_id()).get();
    }else{
        newUser = new User();
    }

    userRegisterUpdate(tempUser, newUser); // Clean up temp user

    return ResponseEntity.ok("Email verified! Redirecting to login...");
    }

    private void userRegisterUpdate(TempUser tempUser, User newUser) {
        newUser.setFirstName(tempUser.getFirstName());
        newUser.setLastName(tempUser.getLastName());
        newUser.setEmail(tempUser.getEmail());
        newUser.setPasswordHash(tempUser.getPassword());
        newUser.setAuthProvider("local");

        userRepo.save(newUser);
        tempUserRepository.delete(tempUser);
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


    // Shared method for processing OAuth logins (Google/Outlook)
    @Transactional
    private AuthResponse handleOAuthLogin(Map<String, Object> payload, String provider) {
        
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
            String token = jwtUtil.generateToken(user);
            
            return mapToAuthResponse(user, token);
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
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
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

    @Transactional
    public AuthResponse updateProfile(Map<String, String> profileData, String email) {
          User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        
        
            user.setFirstName(profileData.get("firstName"));
            user.setLastName(profileData.get("lastName"));
            userRepo.save(user);
            String token = jwtUtil.generateToken(user);
            return mapToAuthResponse(user, token);
    }
    @Transactional
    public String updateEmail(Map<String, String> profileData, String oldEmail) {  
        User user = userRepo.findByEmail(oldEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + oldEmail)); 
        userRepo.findByEmail(profileData.get("newEmail")).ifPresent(u -> {
            throw new RuntimeException("Email already registered");
        });
      
        if(tempUserRepository.existsByEmail(profileData.get("newEmail"))) {
            throw new RuntimeException("Email already pending for verification. Please check your inbox.");
        }
        try{
            TempUser tempUser = new TempUser();
            String token = UUID.randomUUID().toString();
            tempUser.setFirstName(profileData.get("firstName"));
            tempUser.setLastName(profileData.get("lastName"));
            tempUser.setEmail(profileData.get("newEmail"));
            tempUser.setPassword(user.getPasswordHash()); // Keep the same password hash
            tempUser.setUser_id(user.getId());
            tempUser.setToken(token);
            tempUser.setCreatedAt(LocalDateTime.now());
            tempUser.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24hr expiry
            tempUserRepository.save(tempUser);
            emailService.sendVerificationEmail(tempUser.getEmail(), token); 

        }catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database error during email update", e);
        } catch (Exception e) {
            throw new RuntimeException("Email update failed", e);
        }
        

        return "We've sent a verification link to your new email address. Please verify within 24 hours to complete the update.";
        
    }

    @Transactional
    /**
     * Initiates the password reset process by sending a verification email with a token.
     *
     * @param email The user's email address.
     * @throws RuntimeException if the email is not registered or if a reset request is already pending.
     */
    public void forgotPassword(String email) {

         User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));
        
        if(user.getAuthProvider().equals("google") ) {
            throw new RuntimeException("Cannot reset password for OAuth accounts");
        }
        if(tempUserRepository.existsByEmail(email)) {
            throw new RuntimeException("A password reset request is already pending. Please check your email.");
        }
        try{
            String token = UUID.randomUUID().toString();
            TempUser temp = new TempUser();
            temp.setFirstName(user.getFirstName());
            temp.setLastName(user.getLastName());
            temp.setEmail(user.getEmail());
            temp.setToken(token);
            temp.setPassword(user.getPasswordHash());
            temp.setExpiresAt(LocalDateTime.now().plusMinutes((long) 0.5)); // 30 minutes expiry
            temp.setUser_id(user.getId());
            tempUserRepository.save(temp);
            emailService.sendForgotPassword(email, token);

        }catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database error during password reset", e);
        } catch (Exception e) {
            throw new RuntimeException("Password reset failed", e);
        }
        
    }
    

    @Transactional
    /**
     * Resets the user's password using a temporary token.
     *
     * @param token The temporary token sent to the user's email.
     * @param newPassword The new password to set for the user.
     * @throws RuntimeException if the token is invalid, expired, or if the new password is the same as the old one.
     */
    public void resetPassword(String token, String newPassword) {
    
        TempUser tempUser = tempUserRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or Expired token. Try resetting your password again."));

        loggers.info("Resetting password for user: {}", tempUser.getEmail());
        if (tempUser.isExpired()) {
           tempUserRepository.delete(tempUser);
       
        }
        if(passwordEncoder.matches(newPassword, tempUser.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }

        User user = userRepo.findById(tempUser.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tempUserRepository.delete(tempUser); // Clean up temp user

        loggers.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        // TODO Auto-generated method stub
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    
}
