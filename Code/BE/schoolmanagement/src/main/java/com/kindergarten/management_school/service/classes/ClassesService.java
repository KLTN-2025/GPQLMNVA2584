package com.kindergarten.management_school.service.classes;

import com.kindergarten.management_school.dto.request.ClassRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.ClassResponse;

import java.util.List;

public interface ClassesService {
    ApiResponse<ClassResponse> createClass(ClassRequest request);
    ApiResponse<ClassResponse> updateClass(Long id, ClassRequest request);
    ApiResponse<String> deleteClass(Long id);
    ApiResponse<ClassResponse> getClassById(Long id);
    ApiResponse<List<ClassResponse>> getAllClasses();
}
