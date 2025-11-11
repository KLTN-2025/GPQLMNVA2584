package com.kindergarten.management_school.service.attendance;

import com.kindergarten.management_school.dto.request.AttendanceUpdateStatusRequest;
import com.kindergarten.management_school.dto.response.ApiResponse;
import com.kindergarten.management_school.dto.response.AttendanceListItemResponse;
import com.kindergarten.management_school.entity.Attendance;
import com.kindergarten.management_school.entity.Students;
import com.kindergarten.management_school.repository.AttendanceRepository;
import com.kindergarten.management_school.repository.ClassesRepository;
import com.kindergarten.management_school.repository.StudentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentsRepository studentsRepository;
    private final ClassesRepository classesRepository;

    public ApiResponse<?> updateStatus(AttendanceUpdateStatusRequest request) {

        Students student = studentsRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh"));

        Attendance attendance = attendanceRepository
                .findByStudentIdAndAttendanceDate(request.getStudentId(), request.getDate())
                .orElse(null);

        if (attendance == null) {
            attendance = Attendance.builder()
                    .student(student)
                    .clazz(student.getClazz()) // lấy lớp từ student luôn
                    .attendanceDate(request.getDate())
                    .build();
        }

        attendance.setStatus(request.getStatus());
        attendance.setCheckBy(request.getCheckedBy());
        attendance.setCheckTime(LocalDateTime.now());
        attendance.setNote(request.getNote());

        attendanceRepository.save(attendance);

        return ApiResponse.builder()
                .status(200)
                .message("Cập nhật điểm danh thành công!")
                .build();
    }

    @Override
    public ApiResponse<List<AttendanceListItemResponse>> getAttendanceList(Long classId, LocalDate date, String status, String keyword) {

        // lấy all HS trong lớp
        List<Students> students = studentsRepository.findByClazzId(classId);

        // nếu keyword != null -> filter tên
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            students = students.stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(kw))
                    .toList();
        }

        List<AttendanceListItemResponse> result = students.stream().map(s -> {

            // vì attendanceDate là LocalDate => dùng exact date
            Attendance att = attendanceRepository
                    .findByStudentIdAndAttendanceDate(s.getId(), date)
                    .orElse(null);

            // filter status nếu có
            if (status != null && att != null && !status.equals(att.getStatus().name())) {
                return null;
            }

            return AttendanceListItemResponse.builder()
                    .studentId(s.getId())
                    .studentName(s.getFullName())
                    .className(s.getClazz().getClassName())
                    .classCode(s.getClazz().getClassCode())
                    .status(att != null ? att.getStatus().name() : null)
                    .checkedBy(att != null ? att.getCheckBy() : null)
                    .checkTime(att != null ? att.getCheckTime() : null)
                    .note(att != null ? att.getNote() : null)
                    .build();

        }).filter(Objects::nonNull).toList();


        return ApiResponse.<List<AttendanceListItemResponse>>builder()
                .status(200)
                .message("Lấy danh sách điểm danh OK")
                .data(result)
                .build();
    }


}
