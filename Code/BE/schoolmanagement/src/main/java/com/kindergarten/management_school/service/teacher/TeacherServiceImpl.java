package com.kindergarten.management_school.service.teacher;

import com.kindergarten.management_school.dto.request.TeacherRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.TeacherResponse;
import com.kindergarten.management_school.entity.Teacher;
import com.kindergarten.management_school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepo;

    @Override
    public ApiResponse<String> updateTeacher(Long id, TeacherRequest request) {
        try {
            Teacher teacher = teacherRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với ID: " + id));

            teacher.setFullName(request.getFullName());
            teacher.setEmail(request.getEmail());
            teacher.setPhone(request.getPhone());
            teacher.setGender(request.getGender());
            teacher.setSpecialization(request.getSpecialization());
            teacher.setEmergencyContact(request.getEmergencyContact());
            teacher.setDateOfBirth(request.getDateOfBirth());

            teacherRepo.save(teacher);

            log.info("✅ Cập nhật giáo viên ID {} thành công", id);
            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật thông tin giáo viên thành công!")
                    .data(null)
                    .build();

        } catch (ResourceNotFoundException e) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("❌ Lỗi hệ thống khi cập nhật giáo viên: {}", e.getMessage());
            return ApiResponse.<String>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi cập nhật giáo viên.")
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<String> toggleTeacherStatus(Long id) {
        try {
            Teacher teacher = teacherRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với ID: " + id));

            teacher.setIsBlocked(!teacher.getIsBlocked());
            teacherRepo.save(teacher);

            String statusText = teacher.getIsBlocked() ? "đã bị khóa" : "đã được mở khóa";
            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Giáo viên " + teacher.getFullName() + " " + statusText + ".")
                    .data(null)
                    .build();

        } catch (ResourceNotFoundException e) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<List<TeacherResponse>> getAllTeachers() {
        try {
            List<TeacherResponse> list = teacherRepo.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ApiResponse.<List<TeacherResponse>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy danh sách giáo viên thành công.")
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("❌ Lỗi khi lấy danh sách giáo viên: {}", e.getMessage());
            return ApiResponse.<List<TeacherResponse>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy danh sách giáo viên.")
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<TeacherResponse> getTeacherById(Long id) {
        try {
            Teacher teacher = teacherRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với ID: " + id));

            return ApiResponse.<TeacherResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy thông tin giáo viên thành công.")
                    .data(mapToResponse(teacher))
                    .build();
        } catch (ResourceNotFoundException e) {
            return ApiResponse.<TeacherResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .data(null)
                    .build();
        }
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .gender(teacher.getGender())
                .specialization(teacher.getSpecialization())
                .employeeCode(teacher.getEmployeeCode())
                .dateOfBirth(teacher.getDateOfBirth())
                .joinDate(teacher.getJoinDate())
                .isBlocked(teacher.getIsBlocked())
                .avatarUrl(teacher.getAvatarUrl())
                .build();
    }
}
