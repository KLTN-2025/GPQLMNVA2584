package com.kindergarten.management_school.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {
    private Long classId;
    private String title;
    private String description;
    private String createdBy;
}