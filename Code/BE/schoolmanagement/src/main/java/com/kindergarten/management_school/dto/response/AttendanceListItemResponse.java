package com.kindergarten.management_school.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceListItemResponse {
    private Long studentId;
    private String studentName;
    private String className;
    private String classCode;

    private String status;
    private LocalDateTime checkTime;
    private String checkedBy;
    private String note;
}
