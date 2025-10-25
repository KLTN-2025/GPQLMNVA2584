package com.kindergarten.management_school.service.teacher;

import com.kindergarten.management_school.dto.request.TeacherRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.TeacherResponse;

import java.util.List;

public interface TeacherService {
    ApiResponse<String> updateTeacher(Long id, TeacherRequest request);
    ApiResponse<String> toggleTeacherStatus(Long id);
    ApiResponse<List<TeacherResponse>> getAllTeachers();
    ApiResponse<TeacherResponse> getTeacherById(Long id);
}
