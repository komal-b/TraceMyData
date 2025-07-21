package com.tracemydata.dto;
public class TrackerDetectionResult {
    private String trackerName; // Here: "Third-Party Domains"
    private String riskLevel; // "Unknown"
    private String requestUrlsJson;
    private String scriptSnippets;



    public TrackerDetectionResult(String trackerName, String riskLevel, String requestUrlsJson, String scriptSnippets) {
        this.trackerName = trackerName;
        this.riskLevel = riskLevel;
        this.requestUrlsJson = requestUrlsJson;
        this.scriptSnippets = scriptSnippets;
        
    }

     public TrackerDetectionResult(String requestUrlsJson, String scriptSnippets) {
    
        this.requestUrlsJson = requestUrlsJson;
        this.scriptSnippets = scriptSnippets;
        
    }
    public TrackerDetectionResult() {
        
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
    public String getRequestUrlsJson() {
        return requestUrlsJson;
    }
    public void setRequestUrlsJson(String requestUrlsJson) {
        this.requestUrlsJson = requestUrlsJson;
    }
    public String getScriptSnippets() {
        return scriptSnippets;
    }
    public void setScriptSnippets(String scriptSnippets) {
        this.scriptSnippets = scriptSnippets;
    }

}
