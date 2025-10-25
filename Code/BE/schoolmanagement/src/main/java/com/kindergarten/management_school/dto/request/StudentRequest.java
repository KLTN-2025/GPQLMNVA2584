package com.kindergarten.management_school.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String avatarUrl;

    private String studentCode;
    private LocalDateTime enrollmentDate;
    private String healthNotes;
    private String address;

    private Long classId;
    private Long parentId;
}
