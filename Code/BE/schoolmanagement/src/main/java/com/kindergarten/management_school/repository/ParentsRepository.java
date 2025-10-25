package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Parents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface ParentsRepository extends JpaRepository<Parents, Long> {
    @Query("SELECT p FROM Parents p WHERE p.username = :username")
    Optional<Parents> findByAccount_Username(@Param("username") String username);
}
