package com.tracemydata.mapper;

import com.tracemydata.dto.PrivacyRisktDTO;
import com.tracemydata.model.PrivacyRiskScore;

public class PrivacyRiskMapper {

    public static PrivacyRisktDTO toDto(PrivacyRiskScore entity) {
        if (entity == null) {
            return null;
        }
        return new PrivacyRisktDTO(
                entity.getMetadata().getUrl(),
                entity.getRiskScore(),
                entity.getRiskLabel(),
                entity.getRiskTags()
        );
    }
    
}
