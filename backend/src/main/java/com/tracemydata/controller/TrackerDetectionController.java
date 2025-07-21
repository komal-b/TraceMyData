package com.tracemydata.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracemydata.dto.TrackerDetectionResponse;
import com.tracemydata.service.TrackerDetectionService;

@RestController
@RequestMapping("/api/tracker-detection")
public class TrackerDetectionController {

    
    private final TrackerDetectionService trackerDetectionService;

   
    public TrackerDetectionController(TrackerDetectionService trackerDetectionService) {
        this.trackerDetectionService = trackerDetectionService;
    }

    @GetMapping("/detect")
    public ResponseEntity<TrackerDetectionResponse> detectTrackers(@RequestParam String url, @RequestHeader("Authorization") String token) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        TrackerDetectionResponse response = trackerDetectionService.scanAndDetect(url, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getUserTrackerHistory(@RequestHeader("Authorization") String token) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
        

            List<TrackerDetectionResponse> results = trackerDetectionService.getAllTrackers(username);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to fetch tracker history", "details", e.getMessage()));
        }
    }
}
