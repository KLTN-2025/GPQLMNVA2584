package com.kindergarten.management_school.controller.attendance;

import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.AttendanceListItemResponse;
import com.kindergarten.management_school.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AttendanceListItemResponse>>> getAttendanceList(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceList(classId, date, status, keyword)
        );
    }
}
