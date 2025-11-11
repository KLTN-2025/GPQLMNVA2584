package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.AlbumPhotos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface AlbumPhotosRepository extends JpaRepository<AlbumPhotos, Long> {
    List<AlbumPhotos> findByAlbumId(Long albumId);
    void deleteByAlbumId(Long albumId);
}
