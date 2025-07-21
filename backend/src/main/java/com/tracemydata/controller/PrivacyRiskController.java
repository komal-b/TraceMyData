package com.tracemydata.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.tracemydata.dto.PrivacyRisktDTO;
import com.tracemydata.service.PrivacyRiskService;

import io.jsonwebtoken.io.IOException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/privacy-risk")
public class PrivacyRiskController {

    @Autowired
    private PrivacyRiskService riskService;

    @GetMapping("/score")
    public ResponseEntity<?> getRiskScore(@RequestHeader("Authorization") String token) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        List<PrivacyRisktDTO> results;
        try {
            results = riskService.getRiskScore(username);
            return ResponseEntity.ok(results);
        } catch (JsonMappingException e) {
            
            e.printStackTrace();
        } catch (JsonProcessingException e) {
           
            e.printStackTrace();
        }
        
        return ResponseEntity.status(500).body("Error fetching risk score: " );
        


    }
}
