package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    @Query("SELECT t FROM Teacher t WHERE t.username = :username")
    Optional<Teacher> findByAccount_Username(@Param("username") String username);

    @Query("SELECT t.employeeCode FROM Teacher t WHERE t.employeeCode LIKE ?1% ORDER BY t.employeeCode DESC LIMIT 1")
    String findLatestEmployeeCodeByMonth(String yearMonthPrefix);

}
