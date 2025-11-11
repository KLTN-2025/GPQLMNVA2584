package com.kindergarten.management_school.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumPhotoResponse {
    private Long id;
    private Long albumId;
    private String photoUrl;
    private String caption;
    private LocalDateTime uploadedAt;
}