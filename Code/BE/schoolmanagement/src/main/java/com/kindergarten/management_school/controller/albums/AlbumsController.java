package com.kindergarten.management_school.controller.albums;

import com.kindergarten.management_school.dto.request.AlbumApprovalRequest;
import com.kindergarten.management_school.dto.request.AlbumPhotoRequest;
import com.kindergarten.management_school.dto.request.AlbumRequest;
import com.kindergarten.management_school.dto.response.AlbumPhotoResponse;
import com.kindergarten.management_school.dto.response.AlbumResponse;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.service.albums.AlbumsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumsController {

    private final AlbumsService albumsService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(@RequestBody AlbumRequest request) {
        ApiResponse<AlbumResponse> response = albumsService.createAlbum(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/photos")
    public ResponseEntity<ApiResponse<AlbumPhotoResponse>> addPhoto(@RequestBody AlbumPhotoRequest request) {
        ApiResponse<AlbumPhotoResponse> response = albumsService.addPhotoToAlbum(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{albumId}/approval")
    public ResponseEntity<ApiResponse<AlbumResponse>> approveAlbum(
            @PathVariable Long albumId,
            @RequestBody AlbumApprovalRequest request) {
        ApiResponse<AlbumResponse> response = albumsService.approveAlbum(albumId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumById(@PathVariable Long id) {
        ApiResponse<AlbumResponse> response = albumsService.getAlbumById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<AlbumResponse>>> getAlbumsByClass(@PathVariable Long classId) {
        ApiResponse<List<AlbumResponse>> response = albumsService.getAlbumsByClass(classId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AlbumResponse>>> getAlbumsByStatus(@PathVariable String status) {
        ApiResponse<List<AlbumResponse>> response = albumsService.getAlbumsByStatus(status);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{albumId}/photos")
    public ResponseEntity<ApiResponse<List<AlbumPhotoResponse>>> getPhotosByAlbum(@PathVariable Long albumId) {
        ApiResponse<List<AlbumPhotoResponse>> response = albumsService.getPhotosByAlbum(albumId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAlbum(@PathVariable Long id) {
        ApiResponse<String> response = albumsService.deleteAlbum(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<ApiResponse<String>> deletePhoto(@PathVariable Long photoId) {
        ApiResponse<String> response = albumsService.deletePhotoFromAlbum(photoId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}