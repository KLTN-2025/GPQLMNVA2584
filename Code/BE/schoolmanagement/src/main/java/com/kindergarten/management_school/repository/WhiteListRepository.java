package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.WhiteList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.Optional;

@RepositoryRestResource
public interface WhiteListRepository extends JpaRepository<WhiteList, Long> {
    @Query("select wt from WhiteList wt where wt.token = :token")
    Optional<WhiteList> findByToken(@Param("token") String token);

    @Query("select wt from WhiteList wt where wt.userId = :userId")
    Optional<WhiteList> findByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WhiteList w WHERE w.token = :token")
    void deleteByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM WhiteList w WHERE w.expirationToken < :cutoffDate")
    void deleteByExpirationTokenBefore(LocalDateTime cutoffDate);
}
