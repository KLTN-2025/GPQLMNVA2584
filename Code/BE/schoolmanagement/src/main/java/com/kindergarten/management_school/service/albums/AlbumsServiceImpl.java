package com.kindergarten.management_school.service.albums;

import com.kindergarten.management_school.dto.request.AlbumApprovalRequest;
import com.kindergarten.management_school.dto.request.AlbumPhotoRequest;
import com.kindergarten.management_school.dto.request.AlbumRequest;
import com.kindergarten.management_school.dto.response.AlbumPhotoResponse;
import com.kindergarten.management_school.dto.response.AlbumResponse;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.entity.AlbumPhotos;
import com.kindergarten.management_school.entity.Albums;
import com.kindergarten.management_school.entity.Classes;
import com.kindergarten.management_school.repository.AlbumsRepository;
import com.kindergarten.management_school.repository.AlbumPhotosRepository;
import com.kindergarten.management_school.repository.ClassesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumsServiceImpl implements AlbumsService {

    private final AlbumsRepository albumsRepository;
    private final AlbumPhotosRepository albumPhotosRepository;
    private final ClassesRepository classesRepository;

    @Override
    public ApiResponse<AlbumResponse> createAlbum(AlbumRequest request) {
        try {
            // Kiểm tra lớp học tồn tại
            Classes clazz = classesRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học với ID: " + request.getClassId()));

            // Tạo album mới
            Albums album = Albums.builder()
                    .clazz(clazz)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .createdBy(request.getCreatedBy())
                    .status("PENDING") // Mặc định chờ phê duyệt
                    .build();

            Albums savedAlbum = albumsRepository.save(album);

            return ApiResponse.<AlbumResponse>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Tạo album thành công! Đang chờ phê duyệt.")
                    .data(mapToAlbumResponse(savedAlbum))
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AlbumResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi tạo album: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<AlbumPhotoResponse> addPhotoToAlbum(AlbumPhotoRequest request) {
        try {
            // Kiểm tra album tồn tại
            Albums album = albumsRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy album với ID: " + request.getAlbumId()));

            // Kiểm tra album đã được phê duyệt chưa
            if (!"APPROVED".equals(album.getStatus())) {
                return ApiResponse.<AlbumPhotoResponse>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Không thể thêm ảnh vào album chưa được phê duyệt")
                        .data(null)
                        .build();
            }

            // Tạo photo mới
            AlbumPhotos photo = AlbumPhotos.builder()
                    .album(album)
                    .photoUrl(request.getPhotoUrl())
                    .caption(request.getCaption())
                    .build();

            AlbumPhotos savedPhoto = albumPhotosRepository.save(photo);

            return ApiResponse.<AlbumPhotoResponse>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Thêm ảnh vào album thành công!")
                    .data(mapToPhotoResponse(savedPhoto))
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AlbumPhotoResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi thêm ảnh: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<AlbumResponse> approveAlbum(Long albumId, AlbumApprovalRequest request) {
        try {
            Albums album = albumsRepository.findById(albumId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy album với ID: " + albumId));

            // Cập nhật thông tin phê duyệt
            album.setApprovedBy(request.getApprovedBy());
            album.setApprovedAt(LocalDateTime.now());
            album.setStatus(request.getStatus());

            Albums updatedAlbum = albumsRepository.save(album);

            String message = "APPROVED".equals(request.getStatus())
                    ? "Phê duyệt album thành công!"
                    : "Từ chối album thành công!";

            return ApiResponse.<AlbumResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message(message)
                    .data(mapToAlbumResponse(updatedAlbum))
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AlbumResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi phê duyệt album: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AlbumResponse> getAlbumById(Long id) {
        return albumsRepository.findById(id)
                .map(album -> ApiResponse.<AlbumResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy thông tin album thành công!")
                        .data(mapToAlbumResponse(album))
                        .build())
                .orElseGet(() -> ApiResponse.<AlbumResponse>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy album với ID: " + id)
                        .data(null)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AlbumResponse>> getAlbumsByClass(Long classId) {
        List<AlbumResponse> albums = albumsRepository.findByClazzId(classId)
                .stream()
                .map(this::mapToAlbumResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<AlbumResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(albums.isEmpty()
                        ? "Không có album nào cho lớp này"
                        : "Lấy danh sách album theo lớp thành công!")
                .data(albums)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AlbumResponse>> getAlbumsByStatus(String status) {
        List<AlbumResponse> albums = albumsRepository.findByStatus(status)
                .stream()
                .map(this::mapToAlbumResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<AlbumResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(albums.isEmpty()
                        ? "Không có album nào với trạng thái: " + status
                        : "Lấy danh sách album theo trạng thái thành công!")
                .data(albums)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AlbumPhotoResponse>> getPhotosByAlbum(Long albumId) {
        // Kiểm tra album tồn tại
        if (!albumsRepository.existsById(albumId)) {
            return ApiResponse.<List<AlbumPhotoResponse>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tìm thấy album với ID: " + albumId)
                    .data(null)
                    .build();
        }

        List<AlbumPhotoResponse> photos = albumPhotosRepository.findByAlbumId(albumId)
                .stream()
                .map(this::mapToPhotoResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<AlbumPhotoResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách ảnh thành công!")
                .data(photos)
                .build();
    }

    @Override
    public ApiResponse<String> deleteAlbum(Long id) {
        try {
            if (!albumsRepository.existsById(id)) {
                return ApiResponse.<String>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy album với ID: " + id)
                        .data(null)
                        .build();
            }

            albumsRepository.deleteById(id);

            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa album thành công!")
                    .data("Deleted album ID: " + id)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi xóa album: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<String> deletePhotoFromAlbum(Long photoId) {
        try {
            if (!albumPhotosRepository.existsById(photoId)) {
                return ApiResponse.<String>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy ảnh với ID: " + photoId)
                        .data(null)
                        .build();
            }

            albumPhotosRepository.deleteById(photoId);

            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa ảnh thành công!")
                    .data("Deleted photo ID: " + photoId)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi xóa ảnh: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    // Mapping methods
    private AlbumResponse mapToAlbumResponse(Albums album) {
        List<AlbumPhotoResponse> photoResponses = album.getPhotos() != null
                ? album.getPhotos().stream()
                .map(this::mapToPhotoResponse)
                .collect(Collectors.toList())
                : List.of();

        return AlbumResponse.builder()
                .id(album.getId())
                .classId(album.getClazz().getId())
                .className(album.getClazz().getClassName())
                .title(album.getTitle())
                .description(album.getDescription())
                .createdBy(album.getCreatedBy())
                .createdAt(album.getCreatedAt())
                .approvedBy(album.getApprovedBy())
                .approvedAt(album.getApprovedAt())
                .status(album.getStatus())
                .photos(photoResponses)
                .build();
    }

    private AlbumPhotoResponse mapToPhotoResponse(AlbumPhotos photo) {
        return AlbumPhotoResponse.builder()
                .id(photo.getId())
                .albumId(photo.getAlbum().getId())
                .photoUrl(photo.getPhotoUrl())
                .caption(photo.getCaption())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }
}