package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface StudentsRepository extends JpaRepository<Students, Long> {
    @Query("SELECT s FROM Students s WHERE s.username = :username")
    Optional<Students> findByAccount_Username(@Param("username") String username);
}
