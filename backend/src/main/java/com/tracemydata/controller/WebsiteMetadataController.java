package com.tracemydata.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.model.WebsiteMetadata;
import com.tracemydata.service.WebsiteMetadataService;

@RestController
@RequestMapping("/api/metadata")
public class WebsiteMetadataController {

    @Autowired
    private WebsiteMetadataService websiteMetadataService;
    Logger loggers = LoggerFactory.getLogger(WebsiteMetadataController.class);
    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeWebsite(@RequestParam String url, @RequestHeader("Authorization") String token) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            WebsiteMetadata metadata = websiteMetadataService.extractMetadata(url, username);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch metadata", "details", e.getMessage()));
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getMetadataHistory(@RequestHeader("Authorization") String token) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            loggers.info("Fetching metadata history for user: {}", username);
            List<WebsiteMetadataDTO> history = websiteMetadataService.getMetadataHistory(username);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch metadata history", "details", e.getMessage()));
        }
    
    }
}