package com.kindergarten.management_school.service.attendance;

import com.kindergarten.management_school.dto.request.AttendanceUpdateStatusRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.AttendanceListItemResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    ApiResponse<?> updateStatus(AttendanceUpdateStatusRequest request);
    ApiResponse<List<AttendanceListItemResponse>> getAttendanceList(Long classId, LocalDate date, String status, String keyword);

}
