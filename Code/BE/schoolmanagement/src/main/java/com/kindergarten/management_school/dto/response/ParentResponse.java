package com.kindergarten.management_school.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentResponse {
    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String username;
    private String phone;
    private String email;

    private List<String> studentNames;
    private String status;
}
