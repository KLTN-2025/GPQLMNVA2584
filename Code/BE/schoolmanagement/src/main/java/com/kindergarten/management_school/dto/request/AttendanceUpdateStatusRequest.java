package com.kindergarten.management_school.dto.request;

import com.kindergarten.management_school.utils.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceUpdateStatusRequest {
    private Long studentId;
    private Long classId;
    private LocalDate date; // ngày điểm danh
    private AttendanceStatus status;
    private String note;
    private String checkedBy; // ai bấm điểm danh (tên user)
}
