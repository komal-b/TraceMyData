package com.tracemydata.service;

import com.microsoft.playwright.*;
import com.tracemydata.model.WebsiteMetadata;
import com.tracemydata.repository.UserMetadataRepository;
import com.tracemydata.repository.UserRepository;
import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.mapper.WebsiteMetadataMapper;
import com.tracemydata.model.User;
import com.tracemydata.model.UserMetadata;
import com.tracemydata.repository.WebsiteMetadatRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WebsiteMetadataService {

    private final Logger logger = LoggerFactory.getLogger(WebsiteMetadataService.class);

    @Autowired
    private WebsiteMetadatRepository websiteMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    public WebsiteMetadata extractMetadata(String url, String username) {
        // Trim and normalize the URL
        url = normalizeUrl(url.trim());

        // Find the user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Try to find existing metadata
        WebsiteMetadata metadata = websiteMetadataRepository.findByUrl(url);

        if (metadata != null) {
            logger.info("Metadata already exists for URL: {}", url);

            // If metadata is older than 1 month, refresh it
            if (metadata.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(1))) {
                logger.info("Metadata is older than 1 month, refreshing...");
                WebsiteMetadata refreshed = extractWithPlaywright(url, user.getId());
                if (refreshed != null) {
                    refreshed.setId(metadata.getId()); // preserve the same ID
                    metadata = websiteMetadataRepository.save(refreshed);
                    logger.info("Metadata refreshed in database.");
                }
            }

            // Check if user_metadata link exists
            boolean userHasRecord = userMetadataRepository.existsByUser_IdAndMetadata_Id(user.getId(),
                    metadata.getId());

            if (!userHasRecord) {
                userMetadataRepository.save(new UserMetadata(user, metadata));
                logger.info("Added user_metadata link for user {} and URL {}", username, url);
            }

            return metadata;
        }

        // If metadata does not exist at all, extract new metadata
        logger.info("Metadata not found for URL, extracting new metadata: {}", url);
        try {
            WebsiteMetadata newMetadata = extractWithPlaywright(url, user.getId());
            if (newMetadata != null) {
                newMetadata = websiteMetadataRepository.save(newMetadata);
                userMetadataRepository.save(new UserMetadata(user, newMetadata));
                logger.info("Metadata saved and user_metadata link created.");
                return newMetadata;
            }
        } catch (Exception e) {
            logger.error("Failed to extract metadata with Playwright: {}", e.getMessage(), e);
        }

        throw new RuntimeException("Failed to fetch metadata from all sources: " + url);
    }

    // Playwright extraction method
    private WebsiteMetadata extractWithPlaywright(String url, UUID userId) {

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage(new Browser.NewPageOptions()
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                    .setViewportSize(1280, 720));
            page.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9"));

            page.navigate(url, new Page.NavigateOptions().setTimeout(15_000));

            logger.debug("Playwright page: {} ", page);
            logger.debug("Playwright page: {} ", page.content());
            
            String title = page.title();
            String description = getAttr(page, "meta[name=description]", "content");
            if (description == null || description.isBlank()) {
                description = getAttr(page, "meta[name='twitter:description']", "content");
            }else
            if (description == null || description.isBlank()) {
                description = getAttr(page, "meta[property='og:description']", "content");
            }
            String ogTitle = getAttr(page, "meta[property='og:title']", "content");
            String ogDescription = getAttr(page, "meta[property='og:description']", "content");
            if (ogDescription == null || description.isBlank()) {
                ogDescription = getAttr(page, "meta[name='twitter:description']", "content");
            }
            String ogImage = getAttr(page, "meta[property='og:image']", "content");
            String favicon = getAttr(page, "link[rel='icon']", "href");
            logger.info(
                    "Extracted metadata from Playwright: title={}, description={}, ogTitle={}, ogDescription={}, ogImage={}, favicon={}",
                    title, description, ogTitle, ogDescription, ogImage, favicon);

            return new WebsiteMetadata(url, title, description, ogTitle, ogDescription, ogImage, favicon);
        }
    }

    // Helper for Playwright
    private String getAttr(Page page, String selector, String attr) {
        Locator locator = page.locator(selector);
        return locator.count() > 0 ? locator.first().getAttribute(attr) : null;
    }

    public List<WebsiteMetadataDTO> getMetadataHistory(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<UserMetadata> userMetadataList = userMetadataRepository.findByUser(user);

        // Extract WebsiteMetadata from each UserMetadata record
        return userMetadataList.stream()
                .map(um -> WebsiteMetadataMapper.toDto(um.getMetadata()))
                .toList();
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

}
