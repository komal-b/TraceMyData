package com.tracemydata.dto;

import com.tracemydata.model.WebsiteMetadata;

public class WebsiteMetadataDTO {
    private Long id;
    private String url;
    private String title;
    private String description;
    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private String favicon;
    private String createdAt; // We can use String for simpler JSON

    // Constructors
    public WebsiteMetadataDTO() {
    }

    public WebsiteMetadataDTO(Long id, String url, String title, String description,
                              String ogTitle, String ogDescription, String ogImage,
                              String favicon, String createdAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.description = description;
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImage = ogImage;
        this.favicon = favicon;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

  
}
