package com.tracemydata.model;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "tracker_detection")
public class TrackerDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metadata_id", nullable = false)
    private WebsiteMetadata websiteMetadata;

    @Column(name = "detection_date", nullable = false)
    private Instant detectionDate;

    @Column(name = "tracker_name", nullable = false, length = 255)
    private String trackerName;

    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "request_urls", columnDefinition = "TEXT")
    private String requestUrls;

    @Column(name = "script_snippets", columnDefinition = "TEXT")
    private String scriptSnippets;

    // Getters & Setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WebsiteMetadata getWebsiteMetadata() {
        return websiteMetadata;
    }

    public void setWebsiteMetadata(WebsiteMetadata websiteMetadata) {
        this.websiteMetadata = websiteMetadata;
    }

    public Instant getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Instant detectionDate) {
        this.detectionDate = detectionDate;
    }

    public String getTrackerName() {
        return trackerName;
    }

    public void setTrackerName(String trackerName) {
        this.trackerName = trackerName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRequestUrls() {
        return requestUrls;
    }

    public void setRequestUrls(String requestUrls) {
        this.requestUrls = requestUrls;
    }

    public String getScriptSnippets() {
        return scriptSnippets;
    }

    public void setScriptSnippets(String scriptSnippets) {
        this.scriptSnippets = scriptSnippets;
    }
}
