package com.kindergarten.management_school.service.albums;

import com.kindergarten.management_school.dto.request.AlbumApprovalRequest;
import com.kindergarten.management_school.dto.request.AlbumPhotoRequest;
import com.kindergarten.management_school.dto.request.AlbumRequest;
import com.kindergarten.management_school.dto.response.AlbumPhotoResponse;
import com.kindergarten.management_school.dto.response.AlbumResponse;
import com.kindergarten.management_school.dto.response.ApiResponse;

import java.util.List;

public interface AlbumsService {

    // Tạo album mới
    ApiResponse<AlbumResponse> createAlbum(AlbumRequest request);

    // Thêm ảnh vào album
    ApiResponse<AlbumPhotoResponse> addPhotoToAlbum(AlbumPhotoRequest request);

    // Phê duyệt/từ chối album
    ApiResponse<AlbumResponse> approveAlbum(Long albumId, AlbumApprovalRequest request);

    // Lấy album theo ID
    ApiResponse<AlbumResponse> getAlbumById(Long id);

    // Lấy tất cả album theo lớp
    ApiResponse<List<AlbumResponse>> getAlbumsByClass(Long classId);

    // Lấy album theo trạng thái
    ApiResponse<List<AlbumResponse>> getAlbumsByStatus(String status);

    // Lấy tất cả ảnh trong album
    ApiResponse<List<AlbumPhotoResponse>> getPhotosByAlbum(Long albumId);

    // Xóa album
    ApiResponse<String> deleteAlbum(Long id);

    // Xóa ảnh từ album
    ApiResponse<String> deletePhotoFromAlbum(Long photoId);
}