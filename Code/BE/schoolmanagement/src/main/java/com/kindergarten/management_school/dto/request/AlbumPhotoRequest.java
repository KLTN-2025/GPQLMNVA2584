package com.kindergarten.management_school.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumPhotoRequest {
    private Long albumId;
    private String photoUrl;
    private String caption;
}