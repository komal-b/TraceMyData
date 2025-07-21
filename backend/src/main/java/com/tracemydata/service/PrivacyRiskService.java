package com.tracemydata.service;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemydata.dto.PrivacyRiskResponseDTO;
import com.tracemydata.dto.PrivacyRisktDTO;
import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.dto.WebsiteRiskInputDTO;
import com.tracemydata.mapper.PrivacyRiskMapper;
import com.tracemydata.mapper.WebsiteMetadataMapper;
import com.tracemydata.model.PrivacyRiskScore;
import com.tracemydata.model.TrackerDetection;
import com.tracemydata.model.User;
import com.tracemydata.repository.*;

@Service
public class PrivacyRiskService {

    Logger logger = LoggerFactory.getLogger(PrivacyRiskService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Autowired
    private WebsiteMetadatRepository websiteMetadataRepository;

    @Autowired
    private TrackerDetectionRepository trackerDetectionRepository;

    @Autowired
    private PrivacyRiskRepository privacyRiskResponseRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("classpath:/domain_summary.json")
    private Resource domainSummaryJson;

    @Value("classpath:/metadata_keywords.json")
    private Resource metadataKeywordJson;

    private Map<String, Map<String, Object>> loadDomainScores() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(domainSummaryJson.getInputStream(), new TypeReference<>() {});
    }

    private Map<String, List<String>> loadKeywordCategories() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(metadataKeywordJson.getInputStream(), new TypeReference<>() {});
    }

    public List<PrivacyRisktDTO> getRiskScore(String username) throws Exception {
        List<PrivacyRisktDTO> result = new ArrayList<>();

        Map<String, Map<String, Object>> domainScores = loadDomainScores();
        Map<String, List<String>> keywordCategories = loadKeywordCategories();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WebsiteMetadataDTO> websites = userMetadataRepository.findByUser(user).stream()
                .map(um -> WebsiteMetadataMapper.toDto(um.getMetadata()))
                .toList();

        ObjectMapper mapper = new ObjectMapper();
        for (WebsiteMetadataDTO meta : websites) {

            PrivacyRiskScore existingRisk = privacyRiskResponseRepository.findByWebsiteMetadata_Id(meta.getId());
            boolean useOld = false;

            if (existingRisk != null) {
                boolean isFresh = existingRisk.getDetectionDate().isAfter(Instant.now().minusSeconds(30L * 24 * 3600));
                if (isFresh) {
                    result.add(PrivacyRiskMapper.toDto(existingRisk));
                    useOld = true;
                }
            }

            if (useOld) continue;

            List<TrackerDetection> detections = trackerDetectionRepository.findByWebsiteMetadataId(meta.getId());

            Set<String> domainSet = new HashSet<>();
            double trackerRisk = 0;

            for (TrackerDetection d : detections) {
                List<String> urls = mapper.readValue(d.getRequestUrls(), new TypeReference<>() {});
                for (String url : urls) {
                    String domain = extractDomain(url);
                    domainSet.add(domain);
                    Map<String, Object> score = domainScores.getOrDefault(domain, Map.of());
                    trackerRisk += 40 * ((Number) score.getOrDefault("fp", 0)).doubleValue();
                    trackerRisk += 30 * ((Number) score.getOrDefault("cookies", 0)).doubleValue();
                    trackerRisk += 30 * ((Number) score.getOrDefault("prevalence", 0)).doubleValue();
                }
            }

            int numTrackers = domainSet.size();

            String combinedText = (meta.getTitle() + " " + meta.getDescription() + " " +
                    meta.getOgTitle() + " " + meta.getOgDescription()).toLowerCase();

            int metadataRisk = 0;
            for (List<String> keywords : keywordCategories.values()) {
                for (String kw : keywords) {
                    if (combinedText.contains(kw.toLowerCase())) {
                        metadataRisk += 10;
                        break;
                    }
                }
            }

            boolean externalOgImage = meta.getOgImage() != null && !meta.getOgImage().isBlank();
            double finalScore = trackerRisk + metadataRisk;

            WebsiteRiskInputDTO featurePayload = new WebsiteRiskInputDTO(
                    meta.getUrl(),
                    numTrackers,
                    trackerRisk,
                    metadataRisk,
                    externalOgImage,
                    finalScore
            );

            PrivacyRiskResponseDTO responseDTO = getRiskScoreFromModel(featurePayload);

            if (existingRisk == null) {
                existingRisk = new PrivacyRiskScore();
            }

            existingRisk.setMetadata(websiteMetadataRepository.findById(meta.getId()).orElseThrow());
            existingRisk.setRiskScore((int) responseDTO.getRiskScore());
            existingRisk.setRiskLabel(responseDTO.getRiskLabel());
            existingRisk.setDetectionDate(Instant.now());

            privacyRiskResponseRepository.save(existingRisk);
            result.add(PrivacyRiskMapper.toDto(existingRisk));
        }

        return result;
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private PrivacyRiskResponseDTO getRiskScoreFromModel(WebsiteRiskInputDTO input) {
        String url = "http://host.docker.internal:8000/predict-risk/batch";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<WebsiteRiskInputDTO> payload = List.of(input);
        HttpEntity<List<WebsiteRiskInputDTO>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<List<PrivacyRiskResponseDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<PrivacyRiskResponseDTO>>() {}
        );

        List<PrivacyRiskResponseDTO> body = response.getBody();
        if (body == null || body.isEmpty()) {
            throw new RuntimeException("ML service returned empty or null response");
        }

        logger.info("Response from ML service: {}", body.get(0));
        return body.get(0);
    }
}
