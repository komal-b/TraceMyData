package com.tracemydata.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivacyRiskResponseDTO {

    private String url;

    @JsonProperty("risk_score")
    private double riskScore;


    @JsonProperty("risk_label")
    private String riskLabel;

 

    // Getters and setters

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }



    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    

    @Override
    public String toString() {
        return "PrivacyRiskResponseDTO{" +
                "url='" + url + '\'' +
                ", riskScore=" + riskScore +
                ", riskLabel='" + riskLabel + '\'' +
                '}';
    }
}
