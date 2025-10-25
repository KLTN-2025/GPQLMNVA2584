package com.kindergarten.management_school.service.student;

import com.kindergarten.management_school.dto.request.StudentRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {
    ApiResponse<StudentResponse> createStudent(StudentRequest request);
    ApiResponse<StudentResponse> updateStudent(Long id, StudentRequest request);
    ApiResponse<String> deleteStudent(Long id);
    ApiResponse<StudentResponse> getStudentById(Long id);
    ApiResponse<List<StudentResponse>> getAllStudents();
}
