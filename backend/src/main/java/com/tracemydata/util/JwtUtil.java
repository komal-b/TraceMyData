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

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs:86400000}")
    private long jwtExpirationMs;

    @Value("${oauth.google.tokeninfo:https://oauth2.googleapis.com/tokeninfo?id_token=}")
    private String googleTokenInfoUrl;

    @Value("${oauth.outlook.profile:https://graph.microsoft.com/v1.0/me}")
    private String outlookProfileUrl;

    private SecretKey key;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        this.key = Jwts.SIG.HS384.key().build();
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userName", user.getUserName());
        claims.put("authProvider", user.getAuthProvider());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, Jwts.SIG.HS384)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            
            Jwts.parser().verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

    }

    public Map<String, Object> verifyGoogleToken(String idToken) {
        String url = googleTokenInfoUrl + idToken;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Invalid Google ID token");
        }
        return response.getBody();
    }

    public Map<String, Object> verifyOutlookToken(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        httpHeaders.setAll(headers);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(httpHeaders);

        ResponseEntity<Map> response = restTemplate.exchange(outlookProfileUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Invalid Microsoft access token");
        }
        return response.getBody();
    }
}
