package com.kindergarten.management_school.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long id;
    private String className;
    private String classCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String createdBy;
    private List<MenuItemResponse> items;
}
