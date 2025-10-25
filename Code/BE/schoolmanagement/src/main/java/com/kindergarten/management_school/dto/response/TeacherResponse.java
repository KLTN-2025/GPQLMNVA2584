package com.kindergarten.management_school.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String specialization;
    private String employeeCode;
    private LocalDate dateOfBirth;
    private LocalDateTime joinDate;
    private Boolean isBlocked;
    private String avatarUrl;
}
