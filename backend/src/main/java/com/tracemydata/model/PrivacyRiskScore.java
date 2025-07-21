package com.tracemydata.model;

import java.time.Instant;

import com.tracemydata.dto.WebsiteMetadataDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "privacy_risk_score")
public class PrivacyRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "risk_label", nullable = false)
    private String riskLabel;

    @Column(name = "risk_tags", columnDefinition = "TEXT")
    private String riskTags;
     
    
    @OneToOne
    @JoinColumn(name = "metadata_id", nullable = false)
    private WebsiteMetadata websiteMetadata;

    @Column(name = "detection_date", nullable = false)
    private Instant detectionDate;

    public PrivacyRiskScore() {
    }

    public PrivacyRiskScore(WebsiteMetadata websiteMetadata, Instant detectionDate, int riskScore, String riskLabel, String riskTags) {
        this.websiteMetadata = websiteMetadata;
        this.riskScore = riskScore;
        this.riskLabel = riskLabel;
        this.riskTags = riskTags;
        this.detectionDate = detectionDate;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }
    public WebsiteMetadata getMetadata() {
        return websiteMetadata;
    }
    public void setMetadata(WebsiteMetadata websiteMetadata) {
        this.websiteMetadata = websiteMetadata;
    }
    public String getRiskTags() {
        return riskTags;
    }
    public void setRiskTags(String riskTags) {
        this.riskTags = riskTags;
    }

    public Instant getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Instant detectionDate) {
        this.detectionDate = detectionDate;
    }

    @Override
    public String toString() {
        return "PrivacyRiskResponse [riskScore=" + riskScore + ", riskLabel=" + riskLabel + " + , riskTags=" + riskTags + ", metadata=" + websiteMetadata + "]";
    }

   
    
}
