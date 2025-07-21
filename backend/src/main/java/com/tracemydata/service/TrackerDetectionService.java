package com.tracemydata.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracemydata.dto.TrackerDetectionResponse;
import com.tracemydata.dto.TrackerDetectionResult;
import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.mapper.WebsiteMetadataMapper;
import com.tracemydata.model.TrackerDetection;
import com.tracemydata.model.User;
import com.tracemydata.model.UserMetadata;
import com.tracemydata.model.WebsiteMetadata;
import com.tracemydata.repository.TrackerDetectionRepository;
import com.tracemydata.repository.UserMetadataRepository;
import com.tracemydata.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TrackerDetectionService {

    private Logger loggers = LoggerFactory.getLogger(TrackerDetectionService.class);
    @Autowired
    private WebsiteMetadataService websiteMetadataService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Autowired
    private PlaywrightCrawlerService playwrightCrawlerService;

    @Autowired
    private TrackerDetectionRepository trackerDetectionRepository;

    @Transactional
    public TrackerDetectionResponse scanAndDetect(String url, String username) {
        WebsiteMetadata metadata = websiteMetadataService.extractMetadata(url, username);
        WebsiteMetadataDTO metadataDTO = WebsiteMetadataMapper.toDto(metadata); 
        // 1. Check for previous detections  
        url = normalizeUrl(url.trim());
        List<TrackerDetection> existingDetections = trackerDetectionRepository
                .findByWebsiteMetadataId(metadata.getId());

        boolean isFresh = checkOneMonthData(existingDetections);

        if (isFresh && !existingDetections.isEmpty()) {
            loggers.info("Returning cached tracker detections for: {}", url);

            // Convert DB detections to DTO
            List<TrackerDetectionResult> results = existingDetections.stream().map(d -> {
                TrackerDetectionResult dto = new TrackerDetectionResult();
                dto.setTrackerName(d.getTrackerName());
                dto.setRiskLevel(d.getRiskLevel());
                dto.setRequestUrlsJson(d.getRequestUrls());
                dto.setScriptSnippets(d.getScriptSnippets());
                return dto;
            }).toList();

            return new TrackerDetectionResponse(metadataDTO, results);
        }

        // 3. Else: Run fresh detection
        loggers.info("Running new tracker scan for: {}", url);
        List<TrackerDetectionResult> results = detectTrackerResults(url, metadata);

        return new TrackerDetectionResponse(metadataDTO, results);
    }

    private List<TrackerDetectionResult> detectTrackerResults(String url, WebsiteMetadata metadata) {
        List<TrackerDetectionResult> results = playwrightCrawlerService.detectTrackers(url);

        for (TrackerDetectionResult r : results) {
            TrackerDetection detection = new TrackerDetection();
            detection.setWebsiteMetadata(metadata);
            detection.setDetectionDate(Instant.now());
            detection.setTrackerName(r.getTrackerName());
            detection.setRiskLevel(r.getRiskLevel());
            detection.setRequestUrls(r.getRequestUrlsJson());
            detection.setScriptSnippets(r.getScriptSnippets());
            trackerDetectionRepository.save(detection);
        }
        return results;
    }

    private boolean checkOneMonthData(List<TrackerDetection> existingDetections) {
        // 2. Check if data is less than 30 days old
        boolean isFresh = existingDetections.stream()
                .anyMatch(d -> d.getDetectionDate() != null &&
                        d.getDetectionDate().isAfter(Instant.now().minusSeconds(30L * 24 * 3600)));
        return isFresh;
    }

    public List<TrackerDetectionResponse> getAllTrackers(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<UserMetadata> userMetadataList = userMetadataRepository.findByUser(user);

        return userMetadataList.stream()
                .map(um -> {
                    WebsiteMetadata metadata = um.getMetadata();
                    WebsiteMetadataDTO metadataDTO = WebsiteMetadataMapper.toDto(metadata);
                    List<TrackerDetection> detections = ensureDetections(metadata);
                    List<TrackerDetectionResult> results = detections.stream().map(d -> {
                        TrackerDetectionResult dto = new TrackerDetectionResult();
                        dto.setTrackerName(d.getTrackerName());
                        dto.setRiskLevel(d.getRiskLevel());
                        dto.setRequestUrlsJson(d.getRequestUrls());
                        dto.setScriptSnippets(d.getScriptSnippets());
                        return dto;
                    }).toList();
                    return new TrackerDetectionResponse(metadataDTO, results);
                }).toList();
    }

    private List<TrackerDetection> ensureDetections(WebsiteMetadata metadata) {
        List<TrackerDetection> detections = trackerDetectionRepository.findByWebsiteMetadataId(metadata.getId());
        boolean isFresh = checkOneMonthData(detections);
        if (!isFresh || detections.isEmpty()) {
            loggers.info("Running new detection for: {}", metadata.getUrl());
            detectTrackerResults(metadata.getUrl(), metadata);
            return trackerDetectionRepository.findByWebsiteMetadataId(metadata.getId());
        }
        return detections;
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

}
