package com.tracemydata.dto;

public class PrivacyRisktDTO {
    private String url;
    private int riskScore;
    private String riskLabel;
    private String riskTags;


    public PrivacyRisktDTO(String url, int riskScore, String riskLabel, String riskTags) {
        this.url = url;
        this.riskScore = riskScore;
        this.riskLabel = riskLabel;
        this.riskTags = riskTags;
    }

    public PrivacyRisktDTO() {
    }   

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
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

    public String getRiskTags() {
        return riskTags;
    }
    public void setRiskTags(String riskTags) {
        this.riskTags = riskTags;
    }
    // Getters & Setters
}

