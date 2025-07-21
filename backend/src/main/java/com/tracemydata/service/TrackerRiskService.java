package com.tracemydata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemydata.model.TrackerInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class TrackerRiskService {

    private final Map<String, TrackerInfo> trackerMap = new HashMap<>();
    Logger logger = Logger.getLogger(TrackerRiskService.class.getName());

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("domain_summary.json");

        // Read JSON into Map
        TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {
        };
        Map<String, Map<String, Object>> rawMap = mapper.readValue(inputStream, typeRef);

        for (Map.Entry<String, Map<String, Object>> entry : rawMap.entrySet()) {
            String domain = entry.getKey();
            Map<String, Object> data = entry.getValue();

            // Extract fingerprinting score (fp)
            double fp = 0;
            Object fpObj = data.get("fp");
            if (fpObj instanceof Number) {
                fp = ((Number) fpObj).doubleValue();
            }

            String risk;
            if (fp >= 1000) {
                risk = "High";
            } else if (fp >= 100) {
                risk = "Medium";
            } else if (fp >= 1) {
                risk = "Low";
            } else {
                risk = "Unknown";
            }

            TrackerInfo info = new TrackerInfo();
            info.fp = fp;
            info.risk = risk;

            trackerMap.put(domain, info);
        }

        logger.info("Tracker risk map loaded with " + trackerMap.size() + " entries.");
    }

    public String getRisk(String domain) {
        TrackerInfo info = trackerMap.get(domain);
        return info != null ? info.risk : "Unknown";
    }
}
