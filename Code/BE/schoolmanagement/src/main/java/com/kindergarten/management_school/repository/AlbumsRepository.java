package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Albums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface AlbumsRepository extends JpaRepository<Albums, Long> {
    List<Albums> findByClazzId(Long classId);

    List<Albums> findByStatus(String status);

    @Query("SELECT a FROM Albums a WHERE a.clazz.id = :classId AND a.status = :status")
    List<Albums> findByClassIdAndStatus(@Param("classId") Long classId, @Param("status") String status);

    @Query("SELECT COUNT(a) FROM Albums a WHERE a.clazz.id = :classId AND a.status = 'PENDING'")
    long countPendingAlbumsByClassId(@Param("classId") Long classId);
}
