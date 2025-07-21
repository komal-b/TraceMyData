package com.tracemydata.dto;



import java.util.List;

import com.tracemydata.model.WebsiteMetadata;

public class TrackerDetectionResponse {
    private WebsiteMetadataDTO metadata;
    private List<TrackerDetectionResult> detectedTrackers;
    private WebsiteMetadataDTO metadataDTO;

    public TrackerDetectionResponse(WebsiteMetadataDTO metadataDTO, List<TrackerDetectionResult> detectedTrackers) {
        this.metadataDTO = metadataDTO;
        this.detectedTrackers = detectedTrackers;
    }

    public TrackerDetectionResponse(List<TrackerDetectionResult> detectedTrackers) {
        this.detectedTrackers = detectedTrackers;
    }

    // Getters and setters

    public WebsiteMetadataDTO getMetadata() {
        return metadataDTO;
    }

    public void setMetadata(WebsiteMetadataDTO metadataDTO) {
        this.metadataDTO = metadataDTO;
    }

    public List<TrackerDetectionResult> getDetectedTrackers() {
        return detectedTrackers;
    }

    public void setDetectedTrackers(List<TrackerDetectionResult> detectedTrackers) {
        this.detectedTrackers = detectedTrackers;
    }
}
