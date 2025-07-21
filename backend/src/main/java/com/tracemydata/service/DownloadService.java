package com.tracemydata.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.mapper.WebsiteMetadataMapper;
import com.tracemydata.model.PrivacyRiskScore;
import com.tracemydata.model.TrackerDetection;
import com.tracemydata.model.User;

import com.tracemydata.repository.PrivacyRiskRepository;
import com.tracemydata.repository.TrackerDetectionRepository;
import com.tracemydata.repository.UserMetadataRepository;
import com.tracemydata.repository.UserRepository;
import com.tracemydata.repository.WebsiteMetadatRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class DownloadService {

    @Autowired
    private WebsiteMetadatRepository metadataRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrackerDetectionRepository trackerDetectionRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Autowired
    private PrivacyRiskRepository privacyRiskRepository;

    public void writeCsvToResponse(String username, HttpServletResponse response) throws IOException {
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=scan_results.csv");

   List<WebsiteMetadataDTO> websites = getWebsites(username);
    PrintWriter writer = response.getWriter();
    writer.println("ID,URL,Title,Description,Created At,Num Trackers,Trackers,Final Risk Score,Risk Label");

    for (WebsiteMetadataDTO website : websites) {
        List<TrackerDetection> detections = trackerDetectionRepository.findByWebsiteMetadataId(website.getId());
        PrivacyRiskScore riskScore = privacyRiskRepository.findByWebsiteMetadata_Id(website.getId());

        String trackerUrls = detections.stream()
                .map(TrackerDetection::getRequestUrls)
                .reduce((a, b) -> a + " | " + b)
                .orElse("None");

        writer.printf("%s,%s,%s,%s,%s,%d,%s,%s,%s%n",
        website.getId(),
        sanitize(website.getUrl()),
        sanitize(website.getTitle()),
        sanitize(website.getDescription()),
        website.getCreatedAt(),
        detections.size(),
        sanitize(trackerUrls),
        riskScore != null ? riskScore.getRiskScore() : "",
        sanitize(riskScore != null ? riskScore.getRiskLabel() : "")
        );
    }

        writer.flush();
        writer.close();
    }

    


    private List<WebsiteMetadataDTO> getWebsites(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<WebsiteMetadataDTO> websites = userMetadataRepository.findByUser(user).stream()
                .map(um -> WebsiteMetadataMapper.toDto(um.getMetadata()))
                .toList();
        return websites;
    }



        private String sanitize(String input) {
        if (input == null) return "";
        return "\"" + input.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }
}
 




