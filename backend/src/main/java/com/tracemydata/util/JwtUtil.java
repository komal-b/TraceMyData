package com.tracemydata.util;

import com.tracemydata.model.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // JWT secret key (should be at least 256 bits, stored in .env or properties)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // JWT expiration time in milliseconds (default = 24 hrs)
    @Value("${app.jwt.expirationMs:86400000}")
    private long jwtExpirationMs;

    // Google OAuth token verification endpoint
    @Value("${oauth.google.tokeninfo:https://oauth2.googleapis.com/tokeninfo?id_token=}")
    private String googleTokenInfoUrl;

    // Microsoft Graph profile endpoint
    @Value("${oauth.outlook.profile:https://graph.microsoft.com/v1.0/me}")
    private String outlookProfileUrl;

    private SecretKey key; // Symmetric key for signing and verifying tokens
    private final RestTemplate restTemplate = new RestTemplate(); // For REST API calls

    @PostConstruct
    public void init() {
        // Initialize the signing key using HS384 algorithm
        this.key = Jwts.SIG.HS384.key().build();
    }

    // Generate JWT with user data as claims
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("authProvider", user.getAuthProvider());

        return Jwts.builder()
                .claims(claims) // custom claims
                .subject(user.getId().toString()) // subject is user ID
                .issuedAt(new Date()) // token creation time
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // token expiration time
                .signWith(key, Jwts.SIG.HS384) // sign token using HMAC SHA-384
                .compact();
    }

    // Validate the JWT (signature + expiration)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key) // use key to verify signature
                .build()
                .parseSignedClaims(token); // parse claims
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // if token is invalid or tampered
        }
    }

    // Extract user ID (subject) from token
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(); // subject is user ID
    }

    // Verify Google ID token using official Google OAuth API
    public Map<String, Object> verifyGoogleToken(String idToken) {
        String url = googleTokenInfoUrl + idToken;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Invalid Google ID token");
        }
        return response.getBody(); // contains email, name, etc.
    }

}
