package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.BlackList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.Optional;

@RepositoryRestResource
public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    @Query("select bl from BlackList bl where bl.token = :token")
    Optional<BlackList> findByToken(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM BlackList b WHERE b.createdAt < :cutoffDate")
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
