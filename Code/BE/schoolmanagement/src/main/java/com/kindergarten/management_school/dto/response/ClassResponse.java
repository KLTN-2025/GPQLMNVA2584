package com.kindergarten.management_school.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassResponse {
    private Long id;
    private String className;
    private String classCode;
    private String grade;
    private String roomNumber;
    private String academicYear;
    private String teacherName;
}
