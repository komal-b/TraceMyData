package com.tracemydata.repository;

import com.tracemydata.model.User;
import com.tracemydata.model.UserMetadata;
import com.tracemydata.model.WebsiteMetadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMetadataRepository extends JpaRepository<UserMetadata, Long > {

    UserMetadata save(UserMetadata newUserMetadata);
    boolean existsByUser_IdAndMetadata_Id(UUID userId, Long metadataId);
    List<UserMetadata> findByUser(User user);
}
