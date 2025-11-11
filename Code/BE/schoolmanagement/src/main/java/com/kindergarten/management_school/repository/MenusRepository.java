package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Menus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface MenusRepository extends JpaRepository<Menus, Long> {

    @Query("""
        SELECT m FROM Menus m
        WHERE m.clazz.id = :classId
        AND m.startDate >= :startOfWeek
        AND m.endDate <= :endOfWeek
        """)
    List<Menus> findMenuByClassAndWeek(
            @Param("classId") Long classId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

}
