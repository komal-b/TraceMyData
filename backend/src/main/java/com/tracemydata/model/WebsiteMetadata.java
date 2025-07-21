package com.tracemydata.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "website_metadata")
public class WebsiteMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String ogTitle;
    @Column(columnDefinition = "TEXT")
    private String ogDescription;
    @Column(columnDefinition = "TEXT")
    private String ogImage;
    @Column(columnDefinition = "TEXT")
    private String favicon;

    @OneToOne(mappedBy = "websiteMetadata", cascade = CascadeType.ALL)
    private PrivacyRiskScore privacyRiskScore;
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

   

    public WebsiteMetadata() {
        // Default constructor for JPA
}
    public WebsiteMetadata(String url, String title, String description, String ogTitle, String ogDescription, String ogImage, String favicon) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImage = ogImage;
        this.favicon = favicon;
    }
    
    // Getters, Setters, Constructors
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
            this.id = id;
    }
    public String getUrl() {
            return url;
    }
    public void setUrl(String url) {
            this.url = url;
    }
    public String getTitle() {
            return title;
    }
    public void setTitle(String title) {
            this.title = title;
    }
    public String getDescription() {
            return description;
    }
    public void setDescription(String description) {
            this.description = description;
    }
    public String getOgTitle() {
            return ogTitle;
    }
    public void setOgTitle(String ogTitle) {
            this.ogTitle = ogTitle;
    }
    public String getOgDescription() {
            return ogDescription;
    }
    public void setOgDescription(String ogDescription) {
            this.ogDescription = ogDescription;
    }
    public String getOgImage() {
            return ogImage;
    }
    public void setOgImage(String ogImage) {
            this.ogImage = ogImage;
    }
    public String getFavicon() {
            return favicon;
    }
    public void setFavicon(String favicon) {
            this.favicon = favicon;
    }
    public LocalDateTime getCreatedAt() {
            return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
    }   
    public PrivacyRiskScore getPrivacyRiskScore() {
        return privacyRiskScore;
    }
    public void setPrivacyRiskScore(PrivacyRiskScore privacyRiskScore) {
        this.privacyRiskScore = privacyRiskScore;
    }
    
}
