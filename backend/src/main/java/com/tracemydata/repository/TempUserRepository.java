package com.tracemydata.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracemydata.model.TempUser;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser, Long> {
    TempUser save(TempUser tempUser);
    Optional<TempUser> findByToken(String token);
    Optional<TempUser> findByEmail(String email);
    void delete(TempUser tempUser);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
