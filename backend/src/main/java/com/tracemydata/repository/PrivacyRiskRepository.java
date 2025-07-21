package com.tracemydata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracemydata.model.PrivacyRiskScore;

@Repository
public interface PrivacyRiskRepository extends JpaRepository<PrivacyRiskScore, Long> {
    PrivacyRiskScore save(PrivacyRiskScore privacyRiskResponse);
    PrivacyRiskScore findByWebsiteMetadata_Id(Long websiteMetadataId);
    
}
