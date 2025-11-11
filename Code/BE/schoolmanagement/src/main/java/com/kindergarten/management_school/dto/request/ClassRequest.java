package com.kindergarten.management_school.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRequest {
    private String className;
    private String grade;
    private String roomNumber;
    private String academicYear;
    private Long teacherId;
}
