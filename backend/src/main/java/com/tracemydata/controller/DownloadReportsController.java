package com.tracemydata.controller;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracemydata.service.DownloadService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/reports")
public class DownloadReportsController {

    private final DownloadService downloadService;

    public DownloadReportsController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    /**
     * Endpoint to download reports in CSV or PDF format.
     *
     * @param token   Authorization token
     * @param format  Format of the report (csv or pdf)
     * @param response HttpServletResponse to write the file
     * @throws IOException if an I/O error occurs
     */
    
    @GetMapping
    public void exportData(
        @RequestHeader("Authorization") String token,
        @RequestParam("format") String format,
        HttpServletResponse response
    ) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=scan_results.csv");
            downloadService.writeCsvToResponse(username, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format");
        }
    }
}
