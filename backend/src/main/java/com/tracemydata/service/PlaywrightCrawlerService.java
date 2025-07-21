package com.tracemydata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tracemydata.dto.TrackerDetectionResult;

@Service
public class PlaywrightCrawlerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TrackerRiskService trackerRiskService;

    private Logger loggers = LoggerFactory.getLogger(PlaywrightCrawlerService.class);

    public List<TrackerDetectionResult> detectTrackers(String url) {
        Set<String> thirdPartyDomains = new HashSet<>();
        List<String> thirdPartyRequests = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            String mainHost = getHost(url);

            page.onRequest(request -> {
                String reqUrl = request.url();
                String reqHost = getHost(reqUrl);

                if (!reqHost.equals(mainHost)) {
                    thirdPartyDomains.add(reqHost);
                    thirdPartyRequests.add(reqUrl);
                }
            });

            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForTimeout(5000);

            // Risk evaluation logic
            String riskLevel = evaluateRisk(thirdPartyDomains);

            // You can later add logic to extract script snippets from page content
            List<String> scriptSnippets = page.locator("script").allInnerTexts();

            TrackerDetectionResult result = new TrackerDetectionResult(
                    "Third-Party Domains",
                    riskLevel,
                    toJson(thirdPartyRequests),
                    toJson(scriptSnippets));

            loggers.info("Tracker Detection Result:{} ", result.getRequestUrlsJson());

            return Collections.singletonList(result);
        }
    }

    private String evaluateRisk(Set<String> thirdPartyDomains) {
        int high = 0, medium = 0, low = 0;

        for (String domain : thirdPartyDomains) {
            String risk = trackerRiskService.getRisk(domain);

            switch (risk) {
                case "High" -> high++;
                case "Medium" -> medium++;
                case "Low" -> low++;
            }
        }

        if (high > 0)
            return "High";
        else if (medium > 0)
            return "Medium";
        else if (low > 0)
            return "Low";
        else
            return "Unknown";
    }

    private String getHost(String inputUrl) {
        try {
            java.net.URL u = new java.net.URL(inputUrl);
            return u.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
