package com.tracemydata.mapper;

import com.tracemydata.dto.WebsiteMetadataDTO;
import com.tracemydata.model.WebsiteMetadata;

public class WebsiteMetadataMapper {

    public static WebsiteMetadataDTO toDto(WebsiteMetadata entity) {
        if (entity == null) {
            return null;
        }
        return new WebsiteMetadataDTO(
                entity.getId(),
                entity.getUrl(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOgTitle(),
                entity.getOgDescription(),
                entity.getOgImage(),
                entity.getFavicon(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null
        );
    }

    public static WebsiteMetadata toEntity(WebsiteMetadataDTO meta) {
        if (meta == null) {
            return null;
        }
        WebsiteMetadata entity = new WebsiteMetadata(
                meta.getUrl(),
                meta.getTitle(),
                meta.getDescription(),
                meta.getOgTitle(),
                meta.getOgDescription(),
                meta.getOgImage(),
                meta.getFavicon()
        );
        entity.setId(meta.getId());
        return entity;
    }
}
