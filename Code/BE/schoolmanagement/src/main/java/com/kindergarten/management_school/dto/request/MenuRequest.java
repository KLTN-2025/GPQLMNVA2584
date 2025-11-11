package com.kindergarten.management_school.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRequest {
    private Long classId; // id lớp áp dụng menu
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private List<MenuItemRequest> items;
}
