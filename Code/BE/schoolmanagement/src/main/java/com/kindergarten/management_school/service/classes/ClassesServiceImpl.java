package com.kindergarten.management_school.service.classes;

import com.kindergarten.management_school.dto.request.ClassRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.ClassResponse;
import com.kindergarten.management_school.entity.Classes;
import com.kindergarten.management_school.entity.Teacher;
import com.kindergarten.management_school.repository.ClassesRepository;
import com.kindergarten.management_school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassesServiceImpl implements ClassesService {

    private final ClassesRepository classesRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public ApiResponse<ClassResponse> createClass(ClassRequest request) {
        if (classesRepository.existsByClassCode(request.getClassCode())) {
            return ApiResponse.<ClassResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Mã lớp '" + request.getClassCode() + "' đã tồn tại!")
                    .data(null)
                    .build();
        }

        Teacher teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId()).orElse(null);
        }

        Classes newClass = Classes.builder()
                .className(request.getClassName())
                .classCode(request.getClassCode())
                .grade(request.getGrade())
                .roomNumber(request.getRoomNumber())
                .academicYear(request.getAcademicYear())
                .teacher(teacher)
                .build();

        classesRepository.save(newClass);

        return ApiResponse.<ClassResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo lớp học thành công!")
                .data(mapToResponse(newClass))
                .build();
    }

    @Override
    public ApiResponse<ClassResponse> updateClass(Long id, ClassRequest request) {
        return classesRepository.findById(id)
                .map(existing -> {
                    existing.setClassName(request.getClassName());
                    existing.setClassCode(request.getClassCode());
                    existing.setGrade(request.getGrade());
                    existing.setRoomNumber(request.getRoomNumber());
                    existing.setAcademicYear(request.getAcademicYear());

                    if (request.getTeacherId() != null) {
                        Teacher teacher = teacherRepository.findById(request.getTeacherId()).orElse(null);
                        existing.setTeacher(teacher);
                    }

                    classesRepository.save(existing);

                    return ApiResponse.<ClassResponse>builder()
                            .status(HttpStatus.OK.value())
                            .message("Cập nhật lớp học thành công!")
                            .data(mapToResponse(existing))
                            .build();
                })
                .orElseGet(() -> ApiResponse.<ClassResponse>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy lớp có ID: " + id)
                        .data(null)
                        .build());
    }

    @Override
    public ApiResponse<String> deleteClass(Long id) {
        if (!classesRepository.existsById(id)) {
            return ApiResponse.<String>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tồn tại lớp có ID: " + id)
                    .data(null)
                    .build();
        }

        classesRepository.deleteById(id);

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Xoá lớp học thành công!")
                .data("Deleted ID: " + id)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ClassResponse> getClassById(Long id) {
        return classesRepository.findById(id)
                .map(c -> ApiResponse.<ClassResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy thông tin lớp thành công!")
                        .data(mapToResponse(c))
                        .build())
                .orElseGet(() -> ApiResponse.<ClassResponse>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy lớp có ID: " + id)
                        .data(null)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ClassResponse>> getAllClasses() {
        List<ClassResponse> list = classesRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<ClassResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(list.isEmpty()
                        ? "Chưa có lớp học nào trong hệ thống!"
                        : "Lấy danh sách lớp học thành công!")
                .data(list)
                .build();
    }

    private ClassResponse mapToResponse(Classes c) {
        return ClassResponse.builder()
                .id(c.getId())
                .className(c.getClassName())
                .classCode(c.getClassCode())
                .grade(c.getGrade())
                .roomNumber(c.getRoomNumber())
                .academicYear(c.getAcademicYear())
                .teacherName(c.getTeacher() != null ? c.getTeacher().getFullName() : "Chưa có giáo viên")
                .build();
    }
}
