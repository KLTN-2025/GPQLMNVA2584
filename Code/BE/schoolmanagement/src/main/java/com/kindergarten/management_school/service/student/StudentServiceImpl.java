package com.kindergarten.management_school.service.student;

import com.kindergarten.management_school.dto.request.StudentRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.StudentResponse;
import com.kindergarten.management_school.entity.*;
import com.kindergarten.management_school.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentsRepository studentRepository;
    private final ClassesRepository classRepository;
    private final ParentsRepository parentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<StudentResponse> createStudent(StudentRequest request) {
        try {
            Classes clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
            Parents parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phụ huynh"));
            Role studentRole = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò STUDENT"));

            Students student = Students.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .gender(request.getGender())
                    .dateOfBirth(request.getDateOfBirth())
                    .avatarUrl(request.getAvatarUrl())
                    .studentCode(request.getStudentCode())
                    .enrollmentDate(request.getEnrollmentDate())
                    .healthNotes(request.getHealthNotes())
                    .address(request.getAddress())
                    .clazz(clazz)
                    .parent(parent)
                    .role(studentRole)
                    .isBlocked(false)
                    .build();

            Students saved = studentRepository.save(student);
            return ApiResponse.<StudentResponse>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Tạo học sinh thành công")
                    .data(mapToResponse(saved))
                    .build();

        } catch (Exception e) {
            return ApiResponse.<StudentResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi tạo học sinh: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<StudentResponse> updateStudent(Long id, StudentRequest request) {
        try {
            Students student = studentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));

            if (request.getFullName() != null) student.setFullName(request.getFullName());
            if (request.getPhone() != null) student.setPhone(request.getPhone());
            if (request.getGender() != null) student.setGender(request.getGender());
            if (request.getAvatarUrl() != null) student.setAvatarUrl(request.getAvatarUrl());
            if (request.getHealthNotes() != null) student.setHealthNotes(request.getHealthNotes());
            if (request.getAddress() != null) student.setAddress(request.getAddress());
            if (request.getClassId() != null) {
                Classes clazz = classRepository.findById(request.getClassId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
                student.setClazz(clazz);
            }
            if (request.getParentId() != null) {
                Parents parent = parentRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy phụ huynh"));
                student.setParent(parent);
            }

            Students updated = studentRepository.save(student);

            return ApiResponse.<StudentResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật học sinh thành công")
                    .data(mapToResponse(updated))
                    .build();

        } catch (Exception e) {
            return ApiResponse.<StudentResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi khi cập nhật học sinh: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<String> deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tìm thấy học sinh")
                    .data(null)
                    .build();
        }
        studentRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa học sinh thành công")
                .data("DELETED")
                .build();
    }

    @Override
    public ApiResponse<StudentResponse> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> ApiResponse.<StudentResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy thông tin học sinh thành công")
                        .data(mapToResponse(student))
                        .build())
                .orElseGet(() -> ApiResponse.<StudentResponse>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy học sinh")
                        .data(null)
                        .build());
    }

    @Override
    public ApiResponse<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> list = studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<StudentResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách học sinh")
                .data(list)
                .build();
    }

    private StudentResponse mapToResponse(Students s) {
        return StudentResponse.builder()
                .id(s.getId())
                .username(s.getUsername())
                .email(s.getEmail())
                .fullName(s.getFullName())
                .phone(s.getPhone())
                .gender(s.getGender())
                .dateOfBirth(s.getDateOfBirth())
                .avatarUrl(s.getAvatarUrl())
                .studentCode(s.getStudentCode())
                .enrollmentDate(s.getEnrollmentDate())
                .healthNotes(s.getHealthNotes())
                .address(s.getAddress())
                .className(s.getClazz() != null ? s.getClazz().getClassName() : null)
                .parentName(s.getParent() != null ? s.getParent().getFullName() : null)
                .build();
    }
}
