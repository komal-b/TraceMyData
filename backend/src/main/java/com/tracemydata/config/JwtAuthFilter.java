package com.tracemydata.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tracemydata.controller.AuthController;
import com.tracemydata.util.JwtUtil;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {


     private Logger loggers = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil; // your service to extract username and validate token
    private final UserDetailsService userDetailsService; // your UserDetailsService

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String authProvider;
       
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7).trim();
        loggers.debug("Extracted JWT: {}", jwt);
        username = jwtUtil.extractUsername(jwt);
        authProvider = jwtUtil.extractAuthProvider(jwt);
        loggers.debug(authProvider);


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if("google".equalsIgnoreCase(authProvider)) {
                 var userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(username)
                    .password("DUMMY_PASSWORD")
                    .authorities("ROLE_USER")
                    .build();
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }   
               
            }
            else{
                var userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
             }
            }
            
        }
       
        filterChain.doFilter(request, response);
    }
}
