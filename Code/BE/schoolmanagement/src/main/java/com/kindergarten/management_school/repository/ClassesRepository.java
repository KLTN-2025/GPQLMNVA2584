package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface ClassesRepository extends JpaRepository<Classes, Long> {
    Optional<Classes> findByClassCode(String classCode);
    boolean existsByClassCode(String classCode);
}
