package com.kindergarten.management_school.controller.teacher;

import com.kindergarten.management_school.dto.request.TeacherRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.TeacherResponse;
import com.kindergarten.management_school.service.teacher.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateTeacher(
            @PathVariable Long id,
            @RequestBody TeacherRequest request
    ) {
        ApiResponse<String> response = teacherService.updateTeacher(id, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleTeacherStatus(@PathVariable Long id) {
        ApiResponse<String> response = teacherService.toggleTeacherStatus(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAllTeachers() {
        ApiResponse<List<TeacherResponse>> response = teacherService.getAllTeachers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/find/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {
        ApiResponse<TeacherResponse> response = teacherService.getTeacherById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
