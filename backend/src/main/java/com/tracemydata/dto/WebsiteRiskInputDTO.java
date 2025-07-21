package com.tracemydata.dto;

public class WebsiteRiskInputDTO {
    private String url;
    private int numTrackers;
    private double trackerRisk;
    private int metadataRisk;
    private boolean externalOgImage;
    private double finalScore;
    // Constructors
    public WebsiteRiskInputDTO() {
    }   
    public WebsiteRiskInputDTO(String url, int numTrackers, double trackerRisk, int metadataRisk,
                               boolean externalOgImage, double finalScore) {
        this.url = url;
        this.numTrackers = numTrackers;
        this.trackerRisk = trackerRisk;
        this.metadataRisk = metadataRisk;
        this.externalOgImage = externalOgImage;
        this.finalScore = finalScore;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getNumTrackers() {
        return numTrackers;
    }
    public void setNumTrackers(int numTrackers) {
        this.numTrackers = numTrackers;
    }
    public double getTrackerRisk() {
        return trackerRisk;
    }
    public void setTrackerRisk(double trackerRisk) {
        this.trackerRisk = trackerRisk;
    }
    public int getMetadataRisk() {
        return metadataRisk;
    }
    public void setMetadataRisk(int metadataRisk) {
        this.metadataRisk = metadataRisk;
    }
    public boolean isExternalOgImage() {
        return externalOgImage;
    }
    public void setExternalOgImage(boolean externalOgImage) {
        this.externalOgImage = externalOgImage;
    }
    public double getFinalScore() {
        return finalScore;
    }
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }
    @Override
    public String toString() {
        return "WebsiteRiskInputDTO{" +
                "url='" + url + '\'' +
                ", numTrackers=" + numTrackers +
                ", trackerRisk=" + trackerRisk +
                ", metadataRisk=" + metadataRisk +
                ", externalOgImage=" + externalOgImage +
                ", finalScore=" + finalScore +
                '}';
    }
}
