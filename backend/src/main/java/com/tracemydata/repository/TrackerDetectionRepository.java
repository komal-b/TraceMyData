package com.tracemydata.repository;


import com.tracemydata.model.TrackerDetection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackerDetectionRepository extends JpaRepository<TrackerDetection, Long> {
    TrackerDetection save(TrackerDetection detection);
    List<TrackerDetection> findByWebsiteMetadataId(Long websiteMetadataId);
}
