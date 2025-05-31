package com.tracemydata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.tracemydata.repository.TempUserRepository;

import java.time.LocalDateTime;


@Service
public class TempUserCleanupJob {

    private Logger logger = LoggerFactory.getLogger(TempUserCleanupJob.class);


    private final TempUserRepository tempUserRepository;

    public TempUserCleanupJob(TempUserRepository tempUserRepository) {
        this.tempUserRepository = tempUserRepository;
    }

    // Runs every hour (can change to once a day if preferred)
    @Scheduled(cron = "0 0 * * * *") // At minute 0 of every hour
    public void deleteExpiredTempUsers() {
        LocalDateTime now = LocalDateTime.now();
        tempUserRepository.deleteByExpiresAtBefore(now);
        logger.info("Deleted expired temp users at: " + now);
    }
}
