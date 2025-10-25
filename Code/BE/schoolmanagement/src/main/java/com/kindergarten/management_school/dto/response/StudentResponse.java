package com.kindergarten.management_school.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String avatarUrl;

    private String studentCode;
    private LocalDateTime enrollmentDate;
    private String healthNotes;
    private String address;

    private String className;
    private String parentName;
}
