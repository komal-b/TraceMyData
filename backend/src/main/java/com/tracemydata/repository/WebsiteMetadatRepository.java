package com.tracemydata.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracemydata.model.WebsiteMetadata;

@Repository
public interface WebsiteMetadatRepository extends JpaRepository<WebsiteMetadata, Long> {
    WebsiteMetadata save(WebsiteMetadata websiteMetadata);
    WebsiteMetadata findByUrl(String url);
    

}
